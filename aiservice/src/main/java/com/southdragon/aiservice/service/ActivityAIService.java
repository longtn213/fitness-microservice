package com.southdragon.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.southdragon.aiservice.model.Activity;
import com.southdragon.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;

    public String generateRecommendation(Activity activity){
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("AI Response: {}", aiResponse);

        return aiResponse;
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse){
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();
//            log.info("Parsed AI Response: {}", jsonContent);
            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.get("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories:");
            List<String> improvements = extractList(
                    analysisJson.path("improvements"),
                    "area",
                    "recommendation",
                    "No specific improvements provided");
            List<String> suggestions = extractList(
                    analysisJson.path("suggestions"),
                    "workout",
                    "description",
                    "No specific suggestions provided");
            List<String> safety = extractSimpleList(
                    analysisJson.path("safety")
            );

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        }catch(Exception e){
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }

    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractList(JsonNode arrayNode, String keyField, String valueField, String defaultMsg) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return Collections.singletonList(defaultMsg);
        }
        List<String> result = new ArrayList<>();
        arrayNode.forEach(node -> {
            String key = node.path(keyField).asText("");
            String value = node.path(valueField).asText("");
            if (!key.isEmpty() || !value.isEmpty()) {
                result.add(String.format("%s: %s", key, value));
            }
        });
        return result.isEmpty() ? Collections.singletonList(defaultMsg) : result;
    }

    private List<String> extractSimpleList(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return Collections.singletonList("Follow general safety guidelines");
        }
        List<String> list = new ArrayList<>();
        arrayNode.forEach(item -> {
            String text = item.asText("");
            if (!text.isEmpty()) list.add(text);
        });
        return list.isEmpty() ? Collections.singletonList("Follow general safety guidelines") : list;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }
    private String createPromptForActivity(Activity activity) {
        return String.format("""
    Phân tích hoạt động thể chất sau đây và trả về kết quả dưới dạng JSON theo ĐÚNG cấu trúc mẫu dưới đây:

    {
      "analysis": {
        "overall": "Phân tích tổng quan về buổi tập",
        "pace": "Phân tích về tốc độ, nhịp độ luyện tập",
        "heartRate": "Phân tích về nhịp tim",
        "caloriesBurned": "Phân tích về năng lượng tiêu hao (calo)"
      },
      "improvements": [
        {
          "area": "Khu vực hoặc kỹ năng cần cải thiện",
          "recommendation": "Gợi ý chi tiết giúp cải thiện hiệu suất"
        }
      ],
      "suggestions": [
        {
          "workout": "Tên bài tập được khuyến nghị",
          "description": "Mô tả chi tiết cách thực hiện hoặc lý do nên tập bài này"
        }
      ],
      "safety": [
        "Gợi ý an toàn 1",
        "Gợi ý an toàn 2"
      ]
    }

    Phân tích hoạt động:
    • Loại hoạt động: %s
    • Thời lượng: %d phút
    • Lượng calo tiêu hao: %d
    • Thông tin bổ sung: %s

    Hãy trả về nội dung hoàn toàn bằng **tiếng Việt**,
    tập trung vào việc đánh giá hiệu suất, đề xuất cải thiện, gợi ý bài tập tiếp theo và hướng dẫn an toàn.
    Không thêm bất kỳ ký tự nào ngoài JSON.
    """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }

//    private String createPromptForActivity(Activity activity) {
//        return String.format("""
//        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
//        {
//          "analysis": {
//            "overall": "Overall analysis here",
//            "pace": "Pace analysis here",
//            "heartRate": "Heart rate analysis here",
//            "caloriesBurned": "Calories analysis here"
//          },
//          "improvements": [
//            {
//              "area": "Area name",
//              "recommendation": "Detailed recommendation"
//            }
//          ],
//          "suggestions": [
//            {
//              "workout": "Workout name",
//              "description": "Detailed workout description"
//            }
//          ],
//          "safety": [
//            "Safety point 1",
//            "Safety point 2"
//          ]
//        }
//
//        Analyze this activity:
//        Activity Type: %s
//        Duration: %d minutes
//        Calories Burned: %d
//        Additional Metrics: %s
//
//        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
//        Ensure the response follows the EXACT JSON format shown above.
//        """,
//                activity.getType(),
//                activity.getDuration(),
//                activity.getCaloriesBurned(),
//                activity.getAdditionalMetrics()
//        );
//    }
}

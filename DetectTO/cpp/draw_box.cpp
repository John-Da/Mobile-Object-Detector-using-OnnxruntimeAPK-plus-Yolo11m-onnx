#include <opencv2/opencv.hpp>
#include <string>
#include <vector>

struct Detection {
    float x1, y1, x2, y2;
    std::string label;
    float score;
};

cv::Mat drawBoxes(const cv::Mat &image, const std::vector<Detection> &detections) {
    cv::Mat out;
    image.copyTo(out);

    for (const auto &det : detections) {
        cv::rectangle(out,
                      cv::Point(det.x1, det.y1),
                      cv::Point(det.x2, det.y2),
                      cv::Scalar(255, 0, 0), 2); // Blue box

        std::string text = det.label + " " + std::to_string(det.score).substr(0, 4);
        int baseline = 0;
        cv::Size textSize = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseline);
        cv::rectangle(out, cv::Point(det.x1, det.y1 - textSize.height - 5),
                      cv::Point(det.x1 + textSize.width, det.y1), cv::Scalar(255, 0, 0), cv::FILLED);

        cv::putText(out, text, cv::Point(det.x1, det.y1 - 2),
                    cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(255, 255, 255), 1);
    }

    return out;
}

// Custom NMS Filer 

#include <vector>
#include <algorithm>
#include <cmath>

struct Detection {
    float x1, y1, x2, y2; // bbox coordinates
    float score;
    int class_id;
};

// Compute Intersection over Union
float IoU(const Detection &a, const Detection &b) {
    float xx1 = std::max(a.x1, b.x1);
    float yy1 = std::max(a.y1, b.y1);
    float xx2 = std::min(a.x2, b.x2);
    float yy2 = std::min(a.y2, b.y2);

    float w = std::max(0.0f, xx2 - xx1);
    float h = std::max(0.0f, yy2 - yy1);

    float inter = w * h;
    float areaA = (a.x2 - a.x1) * (a.y2 - a.y1);
    float areaB = (b.x2 - b.x1) * (b.y2 - b.y1);

    return inter / (areaA + areaB - inter);
}

// Non-Maximum Suppression
std::vector<Detection> NMS(const std::vector<Detection>& detections, float iouThreshold) {
    std::vector<Detection> result;
    std::vector<Detection> dets = detections;

    // Sort by score descending
    std::sort(dets.begin(), dets.end(), [](const Detection &a, const Detection &b) {
        return a.score > b.score;
    });

    while (!dets.empty()) {
        Detection best = dets[0];
        result.push_back(best);
        std::vector<Detection> tmp;

        for (size_t i = 1; i < dets.size(); ++i) {
            if (IoU(best, dets[i]) < iouThreshold) {
                tmp.push_back(dets[i]);
            }
        }
        dets = tmp;
    }
    return result;
}

// PreviewPage.tsx
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  TextInput,
  ScrollView,
  StyleSheet,
} from "react-native";
import { Slider } from "@miblanchard/react-native-slider";
import { Stack, useLocalSearchParams, useRouter } from "expo-router";
import React, { useState, useEffect } from "react";
import { SafeAreaView } from "react-native-safe-area-context";
import RNPickerSelect from "react-native-picker-select";
import * as FileSystem from "expo-file-system/legacy";
import AsyncStorage from "@react-native-async-storage/async-storage";

import Loading from "@/components/loading";
import { drawBoxesOnImage, loadModel, preprocessImage, runModel } from "@/constants/onnx_runner";

const PreviewPage = () => {
  const { uri } = useLocalSearchParams();
  const router = useRouter();

  const [iou, setIou] = useState<number>(0.2);
  const [labelscale, setLabelScale] = useState<number>(700);
  const [imgHeight, setImgHeight] = useState<string>("640");
  const [imgWidth, setImgWidth] = useState<string>("640");

  const [models, setModels] = useState<{ label: string; value: string }[]>([]);
  const [selectedModel, setSelectedModel] = useState<string | null>(null);

  const [loading, setLoading] = useState(false);

  // load models list (like HomeScreen does)
  const loadModels = async () => {
    const path = await AsyncStorage.getItem("modelsPath");
    if (path) {
      try {
        const files = await FileSystem.readDirectoryAsync(path);
        const onnxFiles = files.filter((f) => f.toLowerCase().endsWith(".onnx"));
        setModels(onnxFiles.map((f) => ({ label: f, value: f })));

        // default: first model selected
        if (!selectedModel && onnxFiles.length > 0) {
          setSelectedModel(onnxFiles[0]);
        }
      } catch (e) {
        console.error("Failed to read models directory:", e);
      }
    }
  };

  useEffect(() => {
    loadModels();
  }, []);

  const handleConfirm = async () => {
  if (!selectedModel) return;
  setLoading(true);

  try {
    const session = await loadModel(selectedModel);
    const inputData = await preprocessImage(uri as string, Number(imgWidth), Number(imgHeight));
    const inferenceResult = await runModel(session, inputData, Number(imgHeight), Number(imgWidth));
    const img_base64 = drawBoxesOnImage(uri, inferenceResult)


      const resultId = Date.now().toString();
      router.push({
        pathname: `/image_details/${resultId}`,
        params: {
          uri,
          selectedModel,
          iou,
          labelscale,
          imgHeight,
          imgWidth,
          imageResult: JSON.stringify({
            detections: inferenceResult,
            image_base64: img_base64,
            width: Number(imgWidth),
            height: Number(imgHeight),
            model_name: selectedModel,
            thresholds: iou,
            font_scale: labelscale,
          }),
        },
      });
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };


  const handleDelete = () => {
    router.push("/(tabs)/");
  };

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />

      <SafeAreaView className="flex-1 bg-[#030014]">
        <ScrollView
          contentContainerStyle={{ paddingBottom: 120 }}
          className="px-5"
        >
          {/* Image Preview */}
          {uri ? (
            <Image
              source={{ uri: uri as string }}
              className="w-full h-80 rounded-2xl mb-8 border border-gray-700"
              resizeMode="cover"
            />
          ) : (
            <Text className="text-white text-center text-lg">No image found</Text>
          )}

          {/* Model Picker */}
          <View className="mb-6">
            <Text className="text-gray-300 font-semibold text-base mb-2">
              Choose Model
            </Text>
            <RNPickerSelect
              onValueChange={(val) => setSelectedModel(val)}
              items={models}
              value={selectedModel}
              style={pickerStyles}
              placeholder={{ label: "Select a model...", value: null }}
            />
          </View>

          {/* IOU Threshold */}
          <View className="mb-6">
            <View className="flex-row justify-between items-center mb-2">
              <Text className="text-gray-300 font-semibold text-base">
                IOU Threshold
              </Text>
              <Text className="text-white">{iou.toFixed(2)}</Text>
            </View>

            <Slider
              value={iou}
              onValueChange={(val) => setIou(val[0])}
              minimumValue={0}
              maximumValue={1}
              step={0.05}
              minimumTrackTintColor="#AB8BFF"
              maximumTrackTintColor="#555"
              thumbTintColor="#AB8BFF"
            />
          </View>

          {/* Label Font Scale */}
          <View className="mb-6">
            <View className="flex-row justify-between items-center mb-2">
              <Text className="text-gray-300 font-semibold text-base">
                Label Scale
              </Text>
              <Text className="text-white">{labelscale.toFixed(0)}</Text>
            </View>

            <Slider
              value={labelscale}
              onValueChange={(val) => setLabelScale(val[0])}
              minimumValue={100}
              maximumValue={1200}
              step={10}
              minimumTrackTintColor="#AB8BFF"
              maximumTrackTintColor="#555"
              thumbTintColor="#AB8BFF"
            />
          </View>

          {/* Image Size */}
          <View className="mb-6">
            <Text className="text-gray-300 mb-2 font-semibold text-base">
              Image Size
            </Text>
            <View className="flex-row gap-6">
              <View className="flex-row gap-2 items-center flex-1">
                <Text className="text-white">h:</Text>
                <TextInput
                  value={imgHeight}
                  onChangeText={setImgHeight}
                  keyboardType="numeric"
                  className="flex-1 bg-[#1a1a2e] text-white px-4 py-3 rounded-xl border border-gray-700"
                />
              </View>
              <View className="flex-row gap-2 items-center flex-1">
                <Text className="text-white">w:</Text>
                <TextInput
                  value={imgWidth}
                  onChangeText={setImgWidth}
                  keyboardType="numeric"
                  className="flex-1 bg-[#1a1a2e] text-white px-4 py-3 rounded-xl border border-gray-700"
                />
              </View>
            </View>
          </View>
        </ScrollView>

        {/* Confirm / Delete Buttons */}
        <View className="absolute bottom-0 left-0 right-0 flex-row justify-between gap-4 px-5 py-4 bg-[#030014] border-t border-gray-800">
          <TouchableOpacity
            onPress={handleDelete}
            className="bg-slate-200 flex-1 h-14 rounded-xl justify-center items-center"
          >
            <Text className="text-red-500 font-semibold text-lg">Delete</Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={handleConfirm}
            className="bg-[#AB8BFF] flex-1 h-14 rounded-xl justify-center items-center"
            disabled={!selectedModel}
          >
            <Text className="text-white font-semibold text-lg">Confirm</Text>
          </TouchableOpacity>
        </View>
        {loading && <Loading isVisible={true} />}
      </SafeAreaView>
    </>
  );
};

export default PreviewPage;

const pickerStyles = StyleSheet.create({
  inputIOS: {
    color: "white",
    backgroundColor: "#1a1a2e",
    padding: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#444",
  },
  inputAndroid: {
    color: "white",
    backgroundColor: "#1a1a2e",
    padding: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#444",
  },
  placeholder: { color: "#999" },
});

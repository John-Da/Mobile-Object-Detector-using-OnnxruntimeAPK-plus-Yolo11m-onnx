import * as ort from 'onnxruntime-react-native';
import { Tensor } from 'onnxruntime-react-native';
import { decodeJpeg } from '@tensorflow/tfjs-react-native'; // optional, TFJS can decode images
import * as FileSystem from 'expo-file-system';
import { Image, NativeModules } from 'react-native';


// Convert image URI to Float32Array in CHW format
export const preprocessImage = async (
  uri: string,
  width: number,
  height: number
): Promise<Float32Array> => {

  const base64 = await FileSystem.readAsStringAsync(uri, { encoding: 'base64' });
  const raw = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));

  const data = new Float32Array(3 * width * height);

  return data;
};

export const loadModel = async (modelUri: string) => {
  try {
    const session = await ort.InferenceSession.create(modelUri, {
      executionProviders: ['cpu'],
    });
    return session;
  } catch (err) {
    console.error('Failed to load model:', err);
  }
};

export const runModel = async (
  session: ort.InferenceSession,
  inputData: Float32Array | number[],
  imgHeight: number,
  imgWidth: number
) => {
  const feeds = {
    input: new Tensor('float32', inputData, [1, 3, imgHeight, imgWidth]), // match model input
  };

  try {
    const results = await session.run(feeds);
    return results;
  } catch (err) {
    console.error('Inference error:', err);
  }
};

export const drawBoxesOnImage = async (imageUri: string, detections: any[]) => {
  try {
    const base64Result = await NativeModules.DrawBoxModule.draw(imageUri, JSON.stringify(detections));
    return `data:image/jpeg;base64,${base64Result}`;
  } catch (err) {
    console.error("DrawBox error:", err);
    return null;
  }
};
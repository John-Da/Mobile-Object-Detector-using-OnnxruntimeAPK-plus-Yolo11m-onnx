import { StyleSheet, Text, TouchableOpacity, ScrollView, View, Platform, Animated } from 'react-native';
import React, { useEffect, useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as FileSystem from "expo-file-system/legacy";
import AsyncStorage from "@react-native-async-storage/async-storage";

// Helper to format path for display
const formatPathForDisplay = (uri: string | null) => {
  if (!uri) return null;
  const system = Platform.OS === 'android' ? 'Android' : 'iOS';
  let path = uri.replace('file://', '');
  const docDir = FileSystem.documentDirectory!.replace('file://', '');
  if (path.includes(docDir)) path = path.replace(docDir, `${system}/data/.../`);
  return path;
};

const SettingItem = ({ title, description, buttonLabel, path, onPress }: any) => (
  <View className='bg-dark-100' style={{ borderRadius: 16, padding: 16, marginBottom: 16 }}>
    <Text style={{ color: 'white', fontSize: 18, fontWeight: '600', marginBottom: 4 }}>{title}</Text>
    <Text style={{ color: '#ccc', fontSize: 14, marginBottom: 4 }}>{description}</Text>
    {path ? (
      <Text style={{ color: '#aaa', fontSize: 12, marginBottom: 8 }}>📂 {path}</Text>
      // <Text style={{ color: '#aaa', fontSize: 12, marginBottom: 8 }}>📂 {formatPathForDisplay(path)}</Text>
      
    ) : (
      <Text style={{ color: '#aaa', fontSize: 12, marginBottom: 8, fontStyle: 'italic' }}>Not set yet</Text>
    )}
    <TouchableOpacity onPress={onPress} className='bg-accent' style={{ padding: 10, borderRadius: 12, alignSelf: 'flex-start' }}>
      <Text style={{ color: 'white', fontWeight: '500' }}>{buttonLabel}</Text>
    </TouchableOpacity>
  </View>
);

const SettingScreen = () => {
  const [modelsPath, setModelsPath] = useState<string | null>(null);
  const [resultsPath, setResultsPath] = useState<string | null>(null);

  const [toastMsg, setToastMsg] = useState<string | null>(null);
  const [fadeAnim] = useState(new Animated.Value(0));

  // Show manual toast
  const showToast = (msg: string) => {
    setToastMsg(msg);
    Animated.timing(fadeAnim, { toValue: 1, duration: 300, useNativeDriver: true }).start(() => {
      setTimeout(() => {
        Animated.timing(fadeAnim, { toValue: 0, duration: 300, useNativeDriver: true }).start(() => setToastMsg(null));
      }, 3500);
    });
  };

  // Setup default folders on first launch
  useEffect(() => {
    const setupFolders = async () => {
      const defaultModels = FileSystem.documentDirectory + 'models/';
      const defaultResults = FileSystem.documentDirectory + 'results/';

      const modelsInfo = await FileSystem.getInfoAsync(defaultModels);
      if (!modelsInfo.exists) await FileSystem.makeDirectoryAsync(defaultModels, { intermediates: true });

      const resultsInfo = await FileSystem.getInfoAsync(defaultResults);
      if (!resultsInfo.exists) await FileSystem.makeDirectoryAsync(defaultResults, { intermediates: true });

      await AsyncStorage.setItem('modelsPath', defaultModels);
      await AsyncStorage.setItem('resultsPath', defaultResults);

      setModelsPath(defaultModels);
      setResultsPath(defaultResults);
    };

    setupFolders();
  }, []);

  // Count files in a folder
  const countFiles = async (folderUri: string) => {
    try {
      const items = await FileSystem.readDirectoryAsync(folderUri);
      return items.length;
    } catch {
      return 0;
    }
  };

  const handleCheckModels = async () => {
    if (!modelsPath) return;
    const count = await countFiles(modelsPath);
    showToast(`${modelsPath} > ${count} items.`);
  };

  const handleCheckResults = async () => {
    if (!resultsPath) return;
    const count = await countFiles(resultsPath);
    showToast(`${resultsPath} > ${count} items.`);
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: '#0f0f0f' }}>
      <ScrollView contentContainerStyle={{ padding: 20, paddingBottom: 100 }}>
        <Text style={{ color: 'white', fontSize: 28, fontWeight: 'bold', marginBottom: 16 }}>Settings</Text>

        <SettingItem
          title="ONNX Models Folder"
          description="Folder inside the app where your ONNX models are stored."
          buttonLabel="Check Models Folder"
          path={modelsPath}
          onPress={handleCheckModels}
        />

        <SettingItem
          title="Results Save Folder"
          description="Folder where results are saved automatically."
          buttonLabel="Check Results Folder"
          path={resultsPath}
          onPress={handleCheckResults}
        />
      </ScrollView>

      {/* Manual toast at bottom */}
      {toastMsg && (
        <Animated.View style={{
          position: 'absolute',
          bottom: 100,
          left: 20,
          right: 20,
          padding: 12,
          backgroundColor: '#AB8BFF',
          borderRadius: 10,
          opacity: fadeAnim,
          alignItems: 'center',
          zIndex: 9999,
          // iOS shadow (spreads evenly in all directions)
          shadowColor: "#AB8BFF",
          shadowOffset: { width: 0, height: 0 },
          shadowOpacity: 0.45,
          shadowRadius: 15,
          // Android shadow
          elevation: 12,
        }}>
          <Text style={{ color: 'white' }}>{toastMsg}</Text>
        </Animated.View>
      )}
    </SafeAreaView>
  );
};

export default SettingScreen;

const styles = StyleSheet.create({});

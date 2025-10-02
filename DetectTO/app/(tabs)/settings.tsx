import { StyleSheet, Text, TouchableOpacity, ScrollView, View, Platform, RefreshControl } from 'react-native';
import React, { useEffect, useState, useCallback } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import AsyncStorage from "@react-native-async-storage/async-storage";
import * as DocumentPicker from "expo-document-picker";

// Helper to format path for display and truncate if too long
const formatPathForDisplay = (uri: string | null) => {
  if (!uri) return null;

  const system = Platform.OS === 'android' ? 'Android' : 'iOS';
  const path = uri.replace('file://', '');

  const parts = path.split('/');

  if (parts.length <= 5) {
    return `${system}: ${path}`;
  } else {
    // Show first 3 parts + ... + last 2 parts
    const first = parts.slice(0, 4).join('/');
    const last = parts.slice(-3).join('/');
    return `${system}: ${first}/.../${last}`;
  }
};

const SettingItem = ({ title, description, buttonLabel, path, onPress }: any) => (
  <View className='bg-dark-100' style={{ borderRadius: 16, padding: 16, marginBottom: 16 }}>
    <Text style={{ color: 'white', fontSize: 18, fontWeight: '600', marginBottom: 4 }}>{title}</Text>
    <Text style={{ color: '#ccc', fontSize: 14, marginBottom: 4 }}>{description}</Text>
    {path ? (
      <Text style={{ color: '#aaa', fontSize: 12, marginBottom: 8 }}>📂 {formatPathForDisplay(path)}</Text>
      // <Text style={{ color: '#aaa', fontSize: 12, marginBottom: 8 }}>📂 {path}</Text>
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
  const [refreshing, setRefreshing] = useState(false);

  const loadSavedPaths = async () => {
    const savedModels = await AsyncStorage.getItem("modelsPath");
    const savedResults = await AsyncStorage.getItem("resultsPath");

    setModelsPath(savedModels);
    setResultsPath(savedResults);
  };

  useEffect(() => {
    loadSavedPaths();
  }, []);

  // Helper to extract parent directory
  const getParentDir = (uri: string) => {
    if (!uri) return null;
    return uri.substring(0, uri.lastIndexOf("/")) + "/";
  };

  const handleSelectModelsFolder = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: "*/*",
        copyToCacheDirectory: false,
      });
      if (result.canceled) return;
      const pickedUri = result.assets[0].uri;
      const folderUri = getParentDir(pickedUri);
      if (folderUri) {
        await AsyncStorage.setItem("modelsPath", folderUri);
        setModelsPath(folderUri);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleSelectResultsFolder = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: "*/*",
        copyToCacheDirectory: false,
      });
      if (result.canceled) return;
      const pickedUri = result.assets[0].uri;
      const folderUri = getParentDir(pickedUri);
      if (folderUri) {
        await AsyncStorage.setItem("resultsPath", folderUri);
        setResultsPath(folderUri);
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <SafeAreaView className='flex-1 bg-primary px-5'>
      <ScrollView
        contentContainerStyle={{ paddingBottom: 100 }}
      >
        <Text style={{ color: 'white', fontSize: 28, fontWeight: 'bold', marginBottom: 16 }}>Settings</Text>

        <SettingItem
          title="ONNX Models Folder"
          description="Choose the folder where your ONNX models are stored."
          buttonLabel="Select Models Folder"
          path={modelsPath}
          onPress={handleSelectModelsFolder}
        />

        <SettingItem
          title="Results Save Folder"
          description="Choose the folder where results should be saved."
          buttonLabel="Select Results Folder"
          path={resultsPath}
          onPress={handleSelectResultsFolder}
        />
      </ScrollView>
    </SafeAreaView>
  );
};

export default SettingScreen;

const styles = StyleSheet.create({});

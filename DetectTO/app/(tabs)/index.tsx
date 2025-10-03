import { StyleSheet, Text, TouchableOpacity, View, FlatList, RefreshControl, BackHandler, Dimensions } from 'react-native';
import React, { useEffect, useState, useCallback, useRef } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as FileSystem from 'expo-file-system/legacy';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Ionicons } from '@expo/vector-icons';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import CustomBottomSheet from '@/components/bottomsheet';


type ModelCardProps = {
  name: string;
  isGrid: boolean;
  onPress?: () => void; // optional
  disabled?: boolean;    // if true, don’t highlight or allow press
};


const { height: SCREEN_HEIGHT } = Dimensions.get("window");


const ModelCard = ({ name, isGrid, onPress, disabled }: ModelCardProps) => {
  const gridStyle = {
    width: 140,  // fixed width
    height: 140, // fixed height → square
    opacity: disabled ? 0.5 : 1,
  };

  const listStyle = {
    height: 54,
    opacity: disabled ? 0.5 : 1,
  };

  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled}
      className={`bg-dark-100 border border-light-100 rounded-2xl p-5 shadow-md shadow-accent/30 ${
        isGrid ? 'items-center justify-center' : 'flex-1'
      }`}
      style={isGrid ? gridStyle : listStyle}
    >
      <Text
        className="text-white font-semibold"
        style={isGrid ? { fontSize: 16, textAlign: 'center' } : { fontSize: 14 }}
        numberOfLines={1}
        ellipsizeMode="middle"
      >
        {name}
      </Text>
    </TouchableOpacity>
  );
};



const HomeScreen = () => {
  const [models, setModels] = useState<string[]>([]);
  const [isGridView, setGridView] = useState(false);
  const [modelsPath, setModelsPath] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const uploadSheetRef = useRef<BottomSheetRefProps>(null);
  const cameraSheetRef = useRef<BottomSheetRefProps>(null);
  const [selectedModel, setSelectedModel] = useState<string | null>(null);

  // Handle hardware back
  useEffect(() => {
    const onBackPress = () => {
      if (uploadSheetRef.current?.isActive()) {
        uploadSheetRef.current.scrollTo(SCREEN_HEIGHT);
        return true;
      }
      if (cameraSheetRef.current?.isActive()) {
        cameraSheetRef.current.scrollTo(SCREEN_HEIGHT);
        return true;
      }
      return false;
    };

    const subscription = BackHandler.addEventListener("hardwareBackPress", onBackPress);

    // Cleanup
    return () => subscription.remove();
  }, []);

  const loadModels = async () => {
    const path = await AsyncStorage.getItem('modelsPath');
    setModelsPath(path);

    if (path) {
      try {
        const files = await FileSystem.readDirectoryAsync(path);
        const onnxFiles = files.filter(file => file.toLowerCase().endsWith('.onnx'));

        if (onnxFiles.length > 0) {
          setModels(onnxFiles);
        } else {
          setModels([]); // empty array → show placeholder separately
        }
      } catch (e) {
        console.error('Failed to read models directory:', e);
        setModels([]);
      }
    } else {
      setModels([]);
    }
  };

  useEffect(() => {
    loadModels();
  }, []);



  const handleSelectModel = (modelName: string) => {
    setSelectedModel(modelName);
    uploadSheetRef.current?.scrollTo(-420);
  };


  const onRefresh = useCallback(async () => {
    if (!modelsPath) return;
    setRefreshing(true);
    try {
      const parent = modelsPath.substring(0, modelsPath.lastIndexOf('/') + 1);
      const files = await FileSystem.readDirectoryAsync(parent);
      const onnxFiles = files.filter(f => f.toLowerCase().endsWith('.onnx'));
      setModels(onnxFiles);
    } catch (e) {
      console.error('Error reading folder:', e);
      setModels([]);
    }
    setRefreshing(false);
  }, [modelsPath]);

  // Handle hardware back
  useEffect(() => {
    const onBackPress = () => {
      if (uploadSheetRef.current?.isActive()) {
        uploadSheetRef.current.scrollTo(SCREEN_HEIGHT);
        return true;
      }
      if (cameraSheetRef.current?.isActive()) {
        cameraSheetRef.current.scrollTo(SCREEN_HEIGHT);
        return true;
      }
      return false;
    };

    const subscription = BackHandler.addEventListener("hardwareBackPress", onBackPress);

    // Cleanup
    return () => subscription.remove();
  }, []);



  return (
    <GestureHandlerRootView className='flex-1'>
    <SafeAreaView className="flex-1 bg-primary px-5">
      <FlatList
        data={models}
        key={isGridView ? 'GRID' : 'LIST'}
        numColumns={isGridView ? 2 : 1}
        columnWrapperStyle={isGridView ? { gap: 12 } : undefined}
        contentContainerStyle={{ paddingBottom: 100, gap: 12 }}
        renderItem={({ item }) => (
          <ModelCard
            name={item}
            isGrid={isGridView}
            onPress={() => handleSelectModel(item)}
          />
        )}
        ListEmptyComponent={
          <View className="items-center justify-center mt-20 px-6">
            <Text
              className="text-white text-lg"
              style={{ textAlign: 'center', lineHeight: 24 }}
            >
              No aviable files found
            </Text>
          </View>
        }
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        ListHeaderComponent={
          <View className="mb-1">
            <View className="flex-row justify-between items-center mb-8">
              <Text style={{ color: 'white', fontSize: 28, fontWeight: 'bold'}}>DetectTO</Text>
              <View className="flex-row gap-4">
                <TouchableOpacity
                  onPress={() => setGridView(true)}
                  className={`${isGridView ? 'bg-accent' : 'bg-dark-100'} px-3 py-2 rounded-lg`}
                >
                  <Ionicons name="grid-outline" size={20} color="white" />
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => setGridView(false)}
                  className={`${!isGridView ? 'bg-accent' : 'bg-dark-100'} px-3 py-2 rounded-lg`}
                >
                  <Ionicons name="list-outline" size={20} color="white" />
                </TouchableOpacity>
              </View>
            </View>

            <Text className="text-light-200 text-center text-lg font-semibold">Models</Text>
          </View>
        }
      />
      {/* BottomSheets */}
      <CustomBottomSheet ref={uploadSheetRef} model={selectedModel} />

    </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default HomeScreen;

const styles = StyleSheet.create({});

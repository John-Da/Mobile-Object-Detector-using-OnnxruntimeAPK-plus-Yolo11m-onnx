import { StyleSheet, Text, TouchableOpacity, View, FlatList } from 'react-native';
import React, { useEffect, useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as FileSystem from 'expo-file-system/legacy';
import AsyncStorage from '@react-native-async-storage/async-storage';

type ModelCardProps = {
  name: string;
  isGrid: boolean;
  onPress: () => void;
};

const ModelCard = ({ name, isGrid, onPress }: ModelCardProps) => (
  <TouchableOpacity
    onPress={onPress}
    className={`bg-dark-100 rounded-2xl p-5 shadow-md shadow-black/30 flex-1 items-center justify-center ${
      isGrid ? '' : 'flex-row justify-between'
    }`}
    style={isGrid ? { minHeight: 100 } : { height: 50 }}
  >
    <Text
      className="text-white font-semibold"
      style={isGrid ? { fontSize: 20 } : { fontSize: 14 }}
    >
      {name}
    </Text>
  </TouchableOpacity>
);

const HomeScreen = () => {
  const [models, setModels] = useState<string[]>([]);
  const [isGridView, setGridView] = useState(false);
  const [modelsPath, setModelsPath] = useState<string | null>(null);

  useEffect(() => {
    const loadModels = async () => {
      const path = await AsyncStorage.getItem('modelsPath');
      setModelsPath(path);

      if (path) {
        const files = await FileSystem.readDirectoryAsync(path);
        setModels(files);
      }
    };

    loadModels();
  }, []);

  const handleSelectModel = (modelName: string) => {
    console.log(`Using model: ${modelName}`);
  };

  return (
    <SafeAreaView className="flex-1 bg-primary">
      <FlatList
        data={models}
        key={isGridView ? 'GRID' : 'LIST'}
        numColumns={isGridView ? 2 : 1}
        columnWrapperStyle={isGridView ? { gap: 12 } : undefined}
        contentContainerStyle={{ padding: 16, paddingBottom: 100, gap: 12 }}
        renderItem={({ item }) => (
          <ModelCard
            name={item}
            isGrid={isGridView}
            onPress={() => handleSelectModel(item)}
          />
        )}
        ListHeaderComponent={
          <View className="mb-6">
            <View className="flex-row justify-between items-center mb-4">
              <Text className="text-white text-3xl font-bold">DetectTO</Text>
              <View className="flex-row gap-4">
                <TouchableOpacity
                  onPress={() => setGridView(true)}
                  className={`${isGridView ? 'bg-accent' : 'bg-dark-100'} px-3 py-2 rounded-lg`}
                >
                  <Text className="text-white">🔲</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => setGridView(false)}
                  className={`${!isGridView ? 'bg-accent' : 'bg-dark-100'} px-3 py-2 rounded-lg`}
                >
                  <Text className="text-white">📃</Text>
                </TouchableOpacity>
              </View>
            </View>

            <Text className="text-light-200 text-lg font-semibold">Models</Text>
          </View>
        }
      />
    </SafeAreaView>
  );
};

export default HomeScreen;

const styles = StyleSheet.create({});

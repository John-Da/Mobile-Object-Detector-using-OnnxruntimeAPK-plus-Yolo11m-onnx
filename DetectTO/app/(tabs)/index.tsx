import { StyleSheet, Text, TouchableOpacity, View, FlatList } from 'react-native'
import React, { useState } from 'react'
import { SafeAreaView } from 'react-native-safe-area-context'


type ModelsCard = {
    name: string;
    isGrid: boolean;
    onPress: () =>  void;
};

const dummyModels = [
  { id: '1', name: 'YOLOv11' },
  { id: '2', name: 'Faster R-CNN' },
  { id: '3', name: 'Mask R-CNN' },
  { id: '4', name: 'DETR' },
];


const ModelCard = ({ name, isGrid, onPress }: ModelsCard) => (
    <TouchableOpacity
        onPress={onPress}
        className={`bg-dark-100 rounded-2xl p-5 shadow-md shadow-black/30 flex-1 items-center justify-center ${
        isGrid ? "" : "flex-row justify-between"
        }`}
        style={isGrid ? { minHeight: 100 } : { height: 50 }}
    >
        <Text className="text-white font-semibold" style={isGrid ? { fontSize:20 } : { fontSize:14 }}>{name}</Text>
    </TouchableOpacity>
);

const HomeScreen = () => {
  const [isGridView, setGridView] = useState(false);

    const handleSelectModel = ({ itemName }: { itemName: string }) => {
        console.log(`Using ${itemName}`);
    };

  return (
    <SafeAreaView className="flex-1 bg-primary">
      <FlatList
        data={dummyModels}
        key={isGridView ? 'GRID' : 'LIST'}
        numColumns={isGridView ? 2 : 1}
        columnWrapperStyle={isGridView ? { gap: 12 } : undefined}
        contentContainerStyle={{ padding: 16, paddingBottom: 100, gap: 12 }}
        renderItem={({ item }) => (
          <ModelCard
            name={item.name}
            isGrid={isGridView}
            onPress={() => handleSelectModel({ itemName: item.name })}
          />
        )}
        ListHeaderComponent={
          <View className="mb-6">
            {/* Header */}
            <View className="flex-row justify-between items-center mb-4">
              <Text className="text-white text-xl font-bold">DetectTO</Text>
              <View className="flex-row gap-4">
                {/* Grid Button */}
                <TouchableOpacity 
                  onPress={() => setGridView(true)} 
                  className={`${isGridView ? 'bg-accent' : 'bg-dark-100'} px-3 py-2 rounded-lg`}
                >
                  <Text className="text-white">🔲</Text>
                </TouchableOpacity>

                {/* List Button */}
                <TouchableOpacity 
                  onPress={() => setGridView(false)} 
                  className={`${!isGridView ? 'bg-accent' : 'bg-dark-100'} px-3 py-2 rounded-lg`}
                >
                  <Text className="text-white">📃</Text>
                </TouchableOpacity>
              </View>
            </View>

            {/* Section Title */}
            <Text className="text-light-200 text-lg font-semibold">Models</Text>
          </View>
        }
      />
    </SafeAreaView>
  );
};

export default HomeScreen;

const styles = StyleSheet.create({});

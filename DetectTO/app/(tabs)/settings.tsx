import { StyleSheet, Text, TouchableOpacity, ScrollView, View } from 'react-native'
import React from 'react'
import { SafeAreaView } from 'react-native-safe-area-context'

const SettingItem = ({ title, description, buttonLabel, onPress }: any) => (
  <View className="bg-dark-100 rounded-2xl p-5 mb-5 shadow-md shadow-black/30">
    <Text className="text-white text-lg font-semibold mb-2">{title}</Text>
    <Text className="text-light-200 text-sm mb-4">{description}</Text>
    <TouchableOpacity 
      onPress={onPress} 
      className="bg-accent px-4 py-2 rounded-xl self-start"
    >
      <Text className="text-white font-medium">{buttonLabel}</Text>
    </TouchableOpacity>
  </View>
);

const SettingScreen = () => {
  return (
    <SafeAreaView className="flex-1 bg-primary">
      <ScrollView contentContainerStyle={{ padding: 20, paddingBottom: 100 }}>
        {/* Page Title */}
        <Text className="text-white text-3xl font-bold mb-6">Settings</Text>

        {/* ONNX Import Path */}
        <SettingItem 
          title="ONNX Import Folder"
          description="Select the folder containing your custom ONNX models (YOLO, Faster-RCNN, Mask-RCNN, DETR)."
          buttonLabel="Set Import Path"
          onPress={() => {}}
        />

        {/* Download Save Path */}
        <SettingItem 
          title="Download Save Path"
          description="Choose where downloaded files and results should be stored."
          buttonLabel="Set Save Path"
          onPress={() => {}}
        />

        {/* Feedback */}
        <SettingItem 
          title="Feedback"
          description="Have suggestions or found a bug? Let us know."
          buttonLabel="Send Feedback"
          onPress={() => {}}
        />
      </ScrollView>
    </SafeAreaView>
  )
}

export default SettingScreen

const styles = StyleSheet.create({})

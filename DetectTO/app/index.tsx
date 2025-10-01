import { StyleSheet, Text, TouchableOpacity, View } from 'react-native'
import React, { useState } from 'react'
import { useRouter } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';

const GetStartedPage = () => {

  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  const toHOME = () => {
    router.push("/(tabs)/");
  }

  return (
    <SafeAreaView className="flex-1 bg-primary items-center justify-center px-6">
      {/* Logo / Hero Placeholder */}
      <View className="w-28 h-28 rounded-full bg-accent/20 items-center justify-center mb-10">
        <Text className="text-accent text-4xl font-bold">D</Text>
      </View>

      {/* Title */}
      <Text className="text-4xl font-extrabold text-white mb-3 text-center">
        Welcome To DetactTO
      </Text>

      {/* Subtitle */}
      <Text className="text-light-200 text-base text-center mb-12 px-4">
        Detect smarter, faster, and easier with{" "}
        <Text className="text-accent font-semibold">AI-powered tools</Text>.
      </Text>

      {/* Get Started Button */}
      <TouchableOpacity
        onPress={toHOME}
        className="bg-accent px-10 py-4 rounded-2xl shadow-lg shadow-accent/30"
      >
        <Text className="text-primary text-lg font-semibold">
          Get Started
        </Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
};

export default GetStartedPage

const styles = StyleSheet.create({})
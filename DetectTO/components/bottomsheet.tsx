import { Dimensions, StyleSheet, Text, TouchableOpacity, View } from 'react-native'
import React, { forwardRef, useCallback, useEffect, useImperativeHandle, useState } from 'react'
import { Gesture, GestureDetector } from 'react-native-gesture-handler'
import Animated, { useAnimatedStyle, useSharedValue, withSpring } from 'react-native-reanimated'
import UploadImageBtn from './upload_image';

export type BottomSheetRefProps = {
  scrollTo: (destination: number) => void;
  isActive: () => boolean;
};

const { height: SCREEN_HEIGHT } = Dimensions.get("window");

const CustomBottomSheet = forwardRef<BottomSheetRefProps>((props, ref) => {
  const context = useSharedValue({ y: 0 });
  const active = useSharedValue(false);

  const translateY = useSharedValue(SCREEN_HEIGHT); 
  const MAX_TRANSLATE_Y = 50; 

  const [tab, setTab] = useState<'photo' | 'camera'>('photo');

  const scrollTo = useCallback((destination: number) => {
    'worklet';
    active.value = destination !== SCREEN_HEIGHT;
    translateY.value = withSpring(destination, { damping: 50 });
  }, []);

  const isActive = useCallback(() => active.value, []);

  useImperativeHandle(ref, () => ({ scrollTo, isActive }), [scrollTo, isActive]);
  // ---- Handle hardware back button ----
    useEffect(() => {
      const onBackPress = () => {
        if (active.value) {
          scrollTo(SCREEN_HEIGHT);
          return true;
        }
        return false;
      };
    }, [scrollTo]);

  const gesture = Gesture.Pan()
    .onStart(() => { context.value = { y: translateY.value }; })
    .onUpdate((event) => {
      translateY.value = event.translationY + context.value.y;
      translateY.value = Math.max(translateY.value, MAX_TRANSLATE_Y);
    })
    .onEnd(() => {
      if (translateY.value > SCREEN_HEIGHT / 2) scrollTo(SCREEN_HEIGHT);
      else scrollTo(MAX_TRANSLATE_Y);
    });

  const rBottomSheetStyle = useAnimatedStyle(() => ({
    transform: [{ translateY: translateY.value }],
  }));

  const rBackdropStyle = useAnimatedStyle(() => {
    return {
      opacity: active.value ? 1 : 0,
      display: active.value ? "flex" : "none", // completely hide when inactive
    };
  });

  return (
    <>

    {/* Backdrop */}
      <Animated.View style={[styles.backdrop, rBackdropStyle]}>
        <TouchableOpacity
          style={{ flex: 1 }}
          activeOpacity={1}
          onPress={() => scrollTo(SCREEN_HEIGHT)} // close when touched outside
        />
      </Animated.View>


      {/* Bottom Sheet */}
      <GestureDetector gesture={gesture}>
        <Animated.View style={[styles.content, rBottomSheetStyle]}>
          {/* drag handle */}
          <View style={styles.line} />

          {/* Tabs */}
          <View style={styles.tabRow}>
            <TouchableOpacity
              style={[styles.tabBtn, tab === 'photo' && styles.activeTab]}
              onPress={() => setTab('photo')}
            >
              <Text style={[styles.tabText, tab === 'photo' && styles.activeTabText]}>Use Photo</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.tabBtn, tab === 'camera' && styles.activeTab]}
              onPress={() => setTab('camera')}
            >
              <Text style={[styles.tabText, tab === 'camera' && styles.activeTabText]}>Use Camera</Text>
            </TouchableOpacity>
          </View>

          {/* Tab content */}
          {tab === 'photo' ? (
            <View style={{ marginTop: 20, width: "100%" }}>
              <UploadImageBtn btn_name="Select from Gallery" mode="gallery" bottomSheetRef={ref} />
              <UploadImageBtn btn_name="Take a Photo" mode="camera" bottomSheetRef={ref} />
            </View>
          ) : (
            <View style={{ marginTop: 20, width: "100%" }}>
              <TouchableOpacity style={styles.optionBtn}>
                <Text style={styles.optionText}>Upload a Video</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.optionBtn}>
                <Text style={styles.optionText}>Open Back Camera</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Cancel */}
          <TouchableOpacity
            style={styles.cancelBtn}
            onPress={() => scrollTo(SCREEN_HEIGHT)}
          >
            <Text style={styles.cancelText}>Cancel</Text>
          </TouchableOpacity>
        </Animated.View>
      </GestureDetector>
    </>
  );
});

export default CustomBottomSheet;

const styles = StyleSheet.create({
  content: {
    flex: 1,
    alignItems: "center",
    padding: 14,
    height: SCREEN_HEIGHT,
    top: SCREEN_HEIGHT,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    position: "absolute",
    left: 0,
    right: 0,
    backgroundColor: "#221f3d", // primary

  },
  line: {
    width: 74,
    height: 4,
    backgroundColor: "#9CA4AB", // light.300
    alignSelf: "center",
    marginVertical: 15,
    borderRadius: 15,
  },
  tabRow: {
    flexDirection: 'row',
    gap: 10,
    width: "100%",
    marginBottom: 10,
  },
  tabBtn: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 12,
    backgroundColor: "#0f0d23", // dark.100
    alignItems: "center",
  },
  activeTab: {
    backgroundColor: "#AB8BFF", // accent
  },
  tabText: {
    fontWeight: "600",
    fontSize: 16,
    color: "#A8B5DB", // light.200
  },
  activeTabText: {
    color: "#030014", // primary
    fontWeight: "bold",
  },
  optionBtn: {
    paddingVertical: 14,
    backgroundColor: "#0f0d23", // dark.200
    borderRadius: 10,
    marginVertical: 6,
    alignItems: "center",
  },
  optionText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#D6C6FF", // light.100
  },
  cancelBtn: {
    marginTop: 20,
    paddingVertical: 12,
    width: "100%",
    borderRadius: 10,
    backgroundColor: "transparent", 
    alignItems: "center",
  },
  cancelText: {
    color: "red",
    fontWeight: "600",
    fontSize: 16,
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: "rgba(0,0,0,0.5)", // dim background
  },
});

import { View, Text, StyleSheet } from 'react-native'
import React from 'react'
import { Tabs } from 'expo-router'


const TabIcon = ({ focused, title}: any) => {
    if (focused){
        return (
            <View style={styles.shadowing} className='absolute bg-accent min-w-[103px] min-h-[43px] size-full justify-center items-center mt-4 rounded-full shadow-md'>
                <Text className='text-base text-white font-bold'>{title}</Text>
            </View>
        )
    }
    return (
        <View className='min-w-[128px] min-h-[53px] size-full justify-center items-center mt-4 rounded-full'>
            <Text className='text-base text-white'>{title}</Text>
        </View>
    )
}

const TabLayout = () => {
    return (
        <Tabs screenOptions={{
            tabBarShowLabel: false,
            tabBarItemStyle: {
                width: '100%',
                height: '100%',
                alignItems: 'center',
                justifyContent: 'center',
            },
            tabBarStyle: {
                borderRadius: 50,
                backgroundColor: "#0f0D23",
                borderWidth: 1,
                borderColor: "#0f0D23",
                position: "absolute",
                marginHorizontal: 20,
                marginBottom: 36,
                height: 52,
                overflow: "hidden",
                // iOS shadow (spreads evenly in all directions)
                shadowColor: "#AB8BFF",
                shadowOffset: { width: 0, height: 0 },
                shadowOpacity: 0.45,
                shadowRadius: 15,
                // Android shadow
                elevation: 5,
            }
        }}>
            <Tabs.Screen name='index' 
                options={{
                    headerShown: false,
                    title: "Home",
                    tabBarIcon: ({ focused }) => (
                        <TabIcon focused={focused} title="Home" />
                    ),
                }} 
            />
            <Tabs.Screen name='settings' options={{
                headerShown: false,
                title: "Settings",
                tabBarIcon: ({ focused }) => (
                    <TabIcon focused={focused} title="Settings"/>
                )
            }} />
        </Tabs>
    )
}

export default TabLayout

const styles = StyleSheet.create({
    shadowing: {
        // iOS shadow (spreads evenly in all directions)
        shadowColor: "#AB8BFF",
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.45,
        shadowRadius: 15,
        // Android shadow
        elevation: 5,
    }
})
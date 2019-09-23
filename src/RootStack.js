import { createStackNavigator } from 'react-navigation-stack';
import Index from "./Index";
import Gesture from "./Gesture";

export const RootStack = createStackNavigator(
    {
      Index: {
          screen:Index,
          navigationOptions:()=>({
              title:'首页'
          })
      },
      Gesture: {
        screen:  Gesture,
        navigationOptions:()=>({
            title:'手势密码'
        })
      },
    },
    {
      initialRouteName: 'Index',
    }
  );
  
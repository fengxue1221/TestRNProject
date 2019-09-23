import React,{Component} from 'react';
import {Text,View,TouchableHighlight,
    NativeModules,Platform,StyleSheet} from 'react-native';

class Index extends Component{
    constructor(props){
        super(props);
        this.fingerCheck=this.fingerCheck.bind(this);
    }

    fingerCheck(){
         if (Platform.OS === 'android') {
        NativeModules.FingerUtil.fingerCheck()
        }
    }

    componentDidMount(){
       
    }
    render(){
        return(
            <View style={styles.container}>
                <TouchableHighlight style={styles.finger}
                    onPress={()=>this.props.navigation.navigate('Gesture')}
                >
                    <Text style={styles.text}>手势密码</Text>
                </TouchableHighlight>
                <TouchableHighlight style={[styles.finger,{marginTop:10}]}
                onPress={this.fingerCheck}
                >
                    <Text style={styles.text}>指纹密码</Text>
                </TouchableHighlight>
            </View>
        );
    }
}

export default Index;

const styles=StyleSheet.create({
    container:{
        flex:1,
        justifyContent:'center',
        alignItems:'center'
    },
    finger:{
        width:200,
        height:60,
        backgroundColor:'#F1744A',
        justifyContent:"center",
        alignItems:'center',
    },
    text:{
        color:'#fff',
        fontSize:18,
    }
});
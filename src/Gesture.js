import React, {Component} from 'react';
import {SafeAreaView} from 'react-navigation';
import PasswordGesture from './gesture/GesturePassword';
import {StyleSheet, Text, TouchableOpacity} from "react-native";

class Gesture extends Component {

    constructor(props) {
        super();
        this.state = {
            status: 'normal',
            message: '请输入手势密码',
            password:'',
            interval:0
        }
    }

    onEnd(pwd) {
        if(this.state.password===''){
            this.setState({
                status:'right',
                message:'请再次输入密码',
                interval:1000,
                password:pwd
            })
        }else if(this.state.password!=''){
            if(this.state.password===pwd){
                this.props.navigation.pop();
            
            }else{
                this.setState({
                    status:'wrong',
                    message:'请重新输入密码',
                    interval:1000
                })
            }
        }
    }

    onStart() {
        this.setState({
            status: 'normal',
        });
    }

    onReset() {
        this.setState({
            status: 'right',
            interval:1000
        });
    }

    render() {
        const {status, message,interval} = this.state;
        return (
            <SafeAreaView style={{flex: 1, backgroundColor: '#F6F6F6'}}>
                <PasswordGesture
                    textStyle={{color: '#FF001F'}}
                    status={status}
                    message={message}
                    interval={interval}
                    onStart={() => this.onStart()}
                    onReset={() => this.onReset()}
                    onEnd={(password) => this.onEnd(password)}
                />
            </SafeAreaView>
        )
    }
}

export default Gesture;

const styles = StyleSheet.create({
    bottom: {
        width: 300,
        height: 120,
        justifyContent: 'center',
        alignItems: 'center',
    }
})

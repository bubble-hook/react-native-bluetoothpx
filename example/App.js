/**
 * Sample React Native App
 *
 * adapted from App.js generated by the following command:
 *
 * react-native init example
 *
 * https://github.com/facebook/react-native
 */

import React, {Component} from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  View,
  NativeEventEmitter,
  NativeModules,
  TouchableOpacity,
} from 'react-native';
import CBBluetoothpx from 'react-native-bluetoothpx';
import {imageLogo} from './data';

export default class App extends Component {
  state = {
    printerState: '',
    error: {},
    devices: [],
  };

  getDevices = () => {
    CBBluetoothpx.getBluetoothList(e => {
      this.setState({
        devices: e.devices,
      });
    });
  };

  componentDidMount() {
    console.log('componentDidMount');

    this.getDevices();

    const eventEmitter = new NativeEventEmitter(CBBluetoothpx);
    eventEmitter.addListener('printerStateChange', e => {
      this.setState({printerState: e});
    });
  }

  connect = device => () => {
    CBBluetoothpx.connect(device.deviceAddress);
  };

  testPrint = () => {
    try {
      CBBluetoothpx.setPaperWidth(384);
      CBBluetoothpx.setLineHeight(40);

      // ['fonts/Tahoma.ttf', 'fonts/THSarabun.ttf'].map(font => {
      //   CBBluetoothpx.setTF(font);

      //   for (let index = 10; index != 0; index = index - 10) {
      //     CBBluetoothpx.setTextSize(index);
      //     CBBluetoothpx.setLineHeight(index + 5);
      //     CBBluetoothpx.addText(font +'  Size :' + index, 1, 0);
      //     CBBluetoothpx.flushText();

      //     CBBluetoothpx.addText('PrinterTest', 1, 0);
      //     CBBluetoothpx.flushText();
      //     CBBluetoothpx.addText('TouchableOpacity', 1, 0);
      //     CBBluetoothpx.flushText();
      //     CBBluetoothpx.addText('ทัสเซเบิลโอบาซิตี้', 1, 0);
      //     CBBluetoothpx.flushText();
      //     CBBluetoothpx.addFeedLine(1);
      //   }

      //   CBBluetoothpx.sendData();
      // });


      

      CBBluetoothpx.addBarCodeWithSize('TK-12130412-UYCdsadsadsadsH',150);
      CBBluetoothpx.sendData();




      // CBBluetoothpx.setTextSize(25);
      // CBBluetoothpx.addText("PrinterTest", 1,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.setTextSize(20);
      // CBBluetoothpx.addText("TouchableOpacity", 1,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.setTextSize(15);
      // CBBluetoothpx.addText("ทัสเซเบิลโอบาซิตี้", 1,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addFeedLine(1);
      // CBBluetoothpx.addBarcode("01209457");
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("บริษัท รุ่งกิจทัวร์ จำกัด", 50,1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("Rungkit Tour Co,.Ltd", 50,1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addFeedLine(1);
      // CBBluetoothpx.addText("ตั๋วโดยสาร", 50,1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("Passenger Ticket", 50,1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addFeedLine(1);
      // CBBluetoothpx.addText("=================================", 50,1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addFeedLine(1);
      // CBBluetoothpx.addText("เลขที่ตั๋ว/Ticket Number", 1,0);
      // CBBluetoothpx.addText("วันเดินทาง/Day", 60,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("TK-12130412", 1,0);
      // CBBluetoothpx.addText("10 มกราคม 2563", 60,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("เวลา/Time", 1,0);
      // CBBluetoothpx.addText("ประเภทรภ/Type", 60,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("12:12", 1,0);
      // CBBluetoothpx.addText("-", 60,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("เลขทะเบียนรถ/Car Number", 1,0);
      // CBBluetoothpx.addText("เลขข้างรถ/Bus Number", 60,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("-", 1,0);
      // CBBluetoothpx.addText("-", 60,0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("PrinterTest", 1,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("TouchableOpacity", 1,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("ทัสเซเบิลโอบาซิตี้", 1,0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addFeedLine(0.2);

      // CBBluetoothpx.setPaperWidth(384);
      // CBBluetoothpx.printBase64ImageStrWithSize(imageLogo,2,140);
      // CBBluetoothpx.addText('บริษัท รุ่งกิจทัวร์ จำกัด', 50, 1);
      // CBBluetoothpx.setTextSize(25);
      // CBBluetoothpx.setLineHeight(35);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.setLineHeight(35);
      // CBBluetoothpx.setTextSize(17);
      // CBBluetoothpx.addText('Rungkit Tour Co,.Ltd', 50, 1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addText('ตั๋วโดยสาร', 50, 1);
      // CBBluetoothpx.setTextSize(25);
      // CBBluetoothpx.setLineHeight(35);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.setLineHeight(35);
      // CBBluetoothpx.setTextSize(17);
      // CBBluetoothpx.addText('Passenger Ticket', 50, 1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addText('เลขที่ตั๋ว/Ticket Number', 1, 0);
      // CBBluetoothpx.addText('วันเดินทาง/Day', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('TK-12130412', 1, 0);
      // CBBluetoothpx.addText('10 มกราคม 2563', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('เวลา/Time', 1, 0);
      // CBBluetoothpx.addText('ประเภทรภ/Type', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('12:12', 1, 0);
      // CBBluetoothpx.addText('-', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('เลขทะเบียนรถ/Car Number', 1, 0);
      // CBBluetoothpx.addText('เลขข้างรถ/Bus Number', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('-', 1, 0);
      // CBBluetoothpx.addText('-', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('ประเภทตั๋ว', 1, 0);
      // CBBluetoothpx.addText('ชื่อผู้จอง/ซื้อ', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('-', 1, 0);
      // CBBluetoothpx.addText('-', 60, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('เส้นทาง/Route', 10, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('ภูเก็ต  -  ชุมพร', 55, 1);
      // CBBluetoothpx.setTextSize(40);
      // CBBluetoothpx.setLineHeight(60);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.setLineHeight(35);
      // CBBluetoothpx.setTextSize(17);
      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('เลขที่นั่ง/Seat No', 10, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('A38', 55, 1);
      // CBBluetoothpx.setTextSize(40);
      // CBBluetoothpx.setLineHeight(60);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.setLineHeight(35);
      // CBBluetoothpx.setTextSize(17);
      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('ราคาตั๋ว', 1, 0);
      // CBBluetoothpx.addText('250.00', 60, 2);
      // CBBluetoothpx.addText('บาท/Baht', 80, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('ส่วนลด', 1, 0);
      // CBBluetoothpx.addText('0.00', 60, 2);
      // CBBluetoothpx.addText('บาท/Baht', 80, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('ยอดรวม', 1, 0);
      // CBBluetoothpx.addText('230.00', 60, 2);
      // CBBluetoothpx.addText('บาท/Baht', 80, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText('ยอดจ่าย', 1, 0);
      // CBBluetoothpx.addText('250.00', 60, 2);
      // CBBluetoothpx.addText('บาท/Baht', 80, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText('เงินทอน', 1, 0);
      // CBBluetoothpx.addText('250.00', 60, 2);
      // CBBluetoothpx.addText('บาท/Baht', 80, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText('ประเภทการชำระ : เงินสด', 1, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addBarcode('RX123G5678');

      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addQRcode('RX123G5678');
      // //CBBluetoothpx.flushText();

      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText('วันที่ออกตั๋ว', 1, 0);
      // CBBluetoothpx.addText('10-10-2020 06:40:00', 30, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('เบอร์โทร', 1, 0);
      // CBBluetoothpx.addText('094-0759080', 20, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText('ผู้ขาย', 1, 0);
      // CBBluetoothpx.addText('ลลิตา แท่นศร', 15, 0);
      // CBBluetoothpx.addText('สาขา', 50, 0);
      // CBBluetoothpx.addText('ท่าภูเก็ต', 70, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addText('คู่ปองทานอาหาร', 50, 1);
      // CBBluetoothpx.setTextSize(40);
      // CBBluetoothpx.setLineHeight(60);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText('มูลค่า 40  บาท', 50, 1);

      // CBBluetoothpx.flushText();
      // CBBluetoothpx.setTextSize(17);
      // CBBluetoothpx.setLineHeight(35);
      // CBBluetoothpx.addText('เมื่อซื้อตั๋วโดยสารมูลค่า 250 บาทขึ้นไป', 50, 1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addText(
      //   '===========================================',
      //   1,
      //   0,
      // );
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.sendData();

      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addCmdFeedLine();

      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addCmdFeedLine();

      // CBBluetoothpx.addText("บริษัท รุ่งกิจทัวร์ จำกัด", 55, 1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("Rungkit Tour Co,.Ltd", 55, 1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addText("ใบเสร็จรับเงิน", 52, 1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("รับฝาก - ส่งของ", 52, 1);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addText("===========================================", 1, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("เลขที่ใบเสร็จ", 1, 0);
      // CBBluetoothpx.addText("วันเวลาที่ฝากส่ง", 60, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("RFX1234567", 1, 0);
      // CBBluetoothpx.addText("10/10/2019 16:30:00", 58, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("สถานที่รับฝาก", 1, 0);
      // CBBluetoothpx.addText("ผู้ทำรายการ", 60, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ภูเก็ต", 1, 0);
      // CBBluetoothpx.addText("ลลิตา แท่นศร", 60, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("===========================================", 1, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ผู้ฝากส่ง", 1, 0);
      // CBBluetoothpx.addText("สมศรี ใจสะอาด", 20, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("เบอร์โทร", 1, 0);
      // CBBluetoothpx.addText("094-0759080", 20, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("===========================================", 1, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("1.", 1, 0);
      // CBBluetoothpx.addText("ผู้รับ สมรัก มานะ", 5, 0);
      // CBBluetoothpx.addText("RX1234567", 70, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ประเภท:", 5, 0);
      // CBBluetoothpx.addText("ทั่วไป", 25, 0);
      // CBBluetoothpx.addText("10kg", 43, 0);
      // CBBluetoothpx.addText("1 ชิ้น", 60, 0);
      // CBBluetoothpx.addText("100.00", 100, 2);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ปลายทาง:", 5, 0);
      // CBBluetoothpx.addText("ตะกั่วป่า", 25, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addCmdFeedLine();
      // CBBluetoothpx.addCmdFeedLine();

      // CBBluetoothpx.addText("2.", 1, 0);
      // CBBluetoothpx.addText("ผู้รับ สมรัก มานะ", 5, 0);
      // CBBluetoothpx.addText("RX1234567", 70, 0);
      // CBBluetoothpx.flushText();
      // CBBluetoothpx.addText("ประเภท:", 5, 0);
      // CBBluetoothpx.addText("ของสด", 25, 0);
      // CBBluetoothpx.addText("5kg", 43, 0);
      // CBBluetoothpx.addText("1 ชิ้น", 60, 0);
      // CBBluetoothpx.addText("120.00", 100, 2);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ปลายทาง:", 5, 0);
      // CBBluetoothpx.addText("ระนอง", 25, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("===========================================", 1, 0);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ยอดรวม", 30, 0);
      // CBBluetoothpx.addText("22,000.00", 100, 2);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ส่วนลด", 30, 0);
      // CBBluetoothpx.addText("10.00", 100, 2);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ยอดสุทธิ", 30, 0);
      // CBBluetoothpx.addText("220.00", 100, 2);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("เงินสด", 30, 0);
      // CBBluetoothpx.addText("159.00", 100, 2);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("เงินทอน", 30, 0);
      // CBBluetoothpx.addText("30.00", 100, 2);
      // CBBluetoothpx.flushText();

      // CBBluetoothpx.addText("ขอบคุณที่ใช้บริการ", 55, 1);
      // CBBluetoothpx.flushText();
    } catch (error) {
      this.setState({error: error});
      alert(error);
    }
  };

  render() {
    return (
      <>
        <View style={styles.container}>
          <Text>PrinterTest</Text>
          <TouchableOpacity
            onPress={this.testPrint}
            style={{backgroundColor: 'red', padding: 10, borderRadius: 10}}>
            <Text>TestPrint</Text>
          </TouchableOpacity>
          <Text>{JSON.stringify(this.state.printerState)}</Text>
          <Text>{JSON.stringify(this.state.error)}</Text>
        </View>
        {this.state.devices.map(b => {
          return (
            <TouchableOpacity
              key={`${b.deviceAddress}`}
              onPress={this.connect(b)}
              style={{
                backgroundColor: 'red',
                padding: 10,
                borderRadius: 10,
                margin: 1,
              }}>
              <Text> {`${b.deviceName}  `}</Text>
              <Text> {`${b.deviceAddress}`}</Text>
            </TouchableOpacity>
          );
        })}
      </>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

import {AppRegistry} from 'react-native';
import PushNotification from 'react-native-push-notification';

import App from './App';
import {name as appName} from './app.json';

PushNotification.configure({
  onNotification: function (notification) {
    console.log('NOTIFICATION:', notification);
    // process the notification
  },
  // (optional) Called when Registered Action is pressed and invokeApp is false, if true onNotification will be called (Android)
  onAction: function (notification) {
    console.log('ACTION:', notification.action);
    console.log('NOTIFICATION:', notification);
    // process the action
  },
  requestPermissions: false,
  popInitialNotification: true,
});

const showNotification = message => {
  PushNotification.createChannel(
    {
      channelId: 'maintenance',
      channelName: 'Mintenance',
      channelDescription: 'A channel to categorise your notifications',
    },
    created => console.log(`createChannel returned '${created}'`),
  );
  PushNotification.localNotification({
    id: 13,
    channelId: 'maintenance',
    title: 'Maintenance Notification',
    message,
    autoCancel: true,
    groupSummary: true,
    actions: ['Yes', 'No'],
  });

  PushNotification.cancelLocalNotifications({id: 13});
};

const MyHeadlessTask = async taskData => {
  //   console.log('BackgroundJob is started!');
  console.log(taskData);
  if (taskData.mqMessage) {
    showNotification(taskData.mqMessage);
  }
};

AppRegistry.registerHeadlessTask('RabbitMq', () => MyHeadlessTask);

AppRegistry.registerComponent(appName, () => App);

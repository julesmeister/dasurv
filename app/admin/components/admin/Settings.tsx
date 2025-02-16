/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useState } from 'react';
import { Form, Button, Checkbox, message, Tabs } from 'antd';
import { AppSettings, defaultSettings, UserRoleSettings } from '@/app/models/settings'; // Adjust the import path as necessary

const Settings = () => {
    const [settings, setSettings] = useState<AppSettings>(defaultSettings);


  const [googleSignInVisible, setGoogleSignInVisible] = useState(true);

  const handleRoleChange = (role: keyof UserRoleSettings) => {
    setSettings(prev => ({
        ...prev,
        userRoles: { ...prev.userRoles, [role]: !prev.userRoles[role] },
    }));
  };
  const handleGoogleSignInVisibilityChange = () => {
    setSettings(prev => ({ ...prev, googleSignInVisible: !prev.googleSignInVisible }));
  };
  const handleSubmit = () => {
    message.success('Settings updated successfully!');
  };

  return (
      <Tabs defaultActiveKey="1">
        <Tabs.TabPane tab="User Roles" key="1">
          <Form onFinish={handleSubmit} layout="vertical">
            <Form.Item label="User Roles">
            <Checkbox
                checked={settings.userRoles.admin}
                onChange={() => handleRoleChange('admin')}
              >Admin</Checkbox>
              <Checkbox
                checked={settings.userRoles.editor}
                onChange={() => handleRoleChange('editor')}
              >Editor</Checkbox>
              <Checkbox
                checked={settings.userRoles.viewer}
                onChange={() => handleRoleChange('viewer')}
              >Viewer</Checkbox>
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit">Update Roles</Button>
            </Form.Item>
          </Form>
        </Tabs.TabPane>
        <Tabs.TabPane tab="Settings" key="2">
          <Form layout="vertical">
            <Form.Item label="Show Google Sign-In Button">
              <Checkbox
                checked={settings.googleSignInVisible}
                onChange={handleGoogleSignInVisibilityChange}
              >Enable</Checkbox>
            </Form.Item>
            <div>
              <Button
                type="primary"
                onClick={() => {
                  // Implement logout logic here
                  console.log('Logout clicked');
                  // Redirect to login page or clear session
                }}
              >
                Logout
              </Button>
            </div>
          </Form>
        </Tabs.TabPane>
      </Tabs>
  );
};

export default Settings;
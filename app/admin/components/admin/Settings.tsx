/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useState } from "react";
import { Form, Button, Checkbox, message, Tabs } from "antd";
import {
  AppSettings,
  defaultSettings,
  UserRoleSettings,
  logout,
  updateSettings,
} from "@/app/models/settings"; // Adjust the import path as necessary

const Settings = () => {
  const [settings, setSettings] = useState<AppSettings>(defaultSettings);
  const settingsId = "your-settings-id"; // Replace with the actual settings ID

  const handleRoleChange = (role: keyof UserRoleSettings) => {
    setSettings((prev) => ({
      ...prev,
      userRoles: { ...prev.userRoles, [role]: !prev.userRoles[role] },
    }));
  };
  const handleGoogleSignInVisibilityChange = async () => {
    const newVisibility = !settings.googleSignInVisible;
    updateSettings(settingsId, { googleSignInVisible: newVisibility });
    setSettings((prev) => ({ ...prev, googleSignInVisible: newVisibility }));
  };
  const handleSubmit = () => {
    message.success("Settings updated successfully!");
  };

  return (
    <Tabs defaultActiveKey="1">
      <Tabs.TabPane tab="User Roles" key="1">
        <Form onFinish={handleSubmit} layout="vertical">
          <Form.Item label="User Roles">
            <Checkbox
              checked={settings.userRoles.admin}
              onChange={() => handleRoleChange("admin")}
            >
              Admin
            </Checkbox>
            <Checkbox
              checked={settings.userRoles.editor}
              onChange={() => handleRoleChange("editor")}
            >
              Editor
            </Checkbox>
            <Checkbox
              checked={settings.userRoles.viewer}
              onChange={() => handleRoleChange("viewer")}
            >
              Viewer
            </Checkbox>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              Update Roles
            </Button>
          </Form.Item>
        </Form>
      </Tabs.TabPane>
      <Tabs.TabPane tab="Settings" key="2">
        <Form layout="vertical">
          <Form.Item label="Show Google Sign-In Button">
            <Checkbox
              checked={settings.googleSignInVisible}
              onChange={handleGoogleSignInVisibilityChange}
            >
              Enable
            </Checkbox>
          </Form.Item>
          <div>
            <Button
              type="primary"
              onClick={async () => {
                console.log("Logout clicked");
                await logout(); // Call the logout function
                // Optionally redirect to the login page or perform other actions
                // For example: router.push('/login');
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

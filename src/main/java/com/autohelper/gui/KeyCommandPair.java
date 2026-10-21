package com.autohelper.gui;

/**
 * 按键-命令对
 * 用于UI中动态管理多个按键和命令的映射关系
 */
public class KeyCommandPair {
    public String keyName;
    public String command;

    public KeyCommandPair() {
        this.keyName = "";
        this.command = "";
    }

    public KeyCommandPair(String keyName, String command) {
        this.keyName = keyName;
        this.command = command;
    }

    public boolean isEmpty() {
        return (keyName == null || keyName.trim().isEmpty())
            && (command == null || command.trim().isEmpty());
    }

    public boolean isValid() {
        return (keyName != null && !keyName.trim().isEmpty())
            || (command != null && !command.trim().isEmpty());
    }
}

import { registerPlugin } from '@capacitor/core';

export const NativeAudio = registerPlugin('NativeAudio');

export function isAndroidApp() {
    return (
        typeof window !== 'undefined' &&
        !!window.Capacitor &&
        typeof window.Capacitor.getPlatform === 'function' &&
        window.Capacitor.getPlatform() === 'android'
    );
}

export async function playNativeAudio(uri, startTime = 0) {
    if (!isAndroidApp()) {
        throw new Error('NativeAudio is only available inside the Android app');
    }

    return NativeAudio.play({
        uri,
        startTime,
    });
}

export async function pauseNativeAudio() {
    if (!isAndroidApp()) return;
    return NativeAudio.pause();
}

export async function resumeNativeAudio() {
    if (!isAndroidApp()) return;
    return NativeAudio.resume();
}

export async function stopNativeAudio() {
    if (!isAndroidApp()) return;
    return NativeAudio.stop();
}

export async function seekNativeAudio(seconds) {
    if (!isAndroidApp()) return;
    return NativeAudio.seek({
        position: seconds,
    });
}

export async function getNativeAudioStatus() {
    if (!isAndroidApp()) {
        return {
            playing: false,
            position: 0,
            duration: 0,
        };
    }

    return NativeAudio.getStatus();
}

export function addNativeAudioEndedListener(callback) {
    if (!isAndroidApp()) return null;

    return NativeAudio.addListener('ended', callback);
}
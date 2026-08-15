export const features = [
  { id: 'qr', icon: '📷', name: 'UPI QR', desc: 'Scan and share UPI QR codes instantly from the floating overlay.' },
  { id: 'translator', icon: '🌐', name: 'Translator', desc: 'Translate text between languages using on-device processing.' },
  { id: 'clipboard', icon: '📋', name: 'Clipboard', desc: 'Quick-access clipboard manager with history and favorites.' },
  { id: 'notes', icon: '📝', name: 'Notes', desc: 'Floating sticky notes that stay on top of other apps.' },
  { id: 'calculator', icon: '🧮', name: 'Calculator', desc: 'Floating calculator for quick arithmetic on any screen.' },
  { id: 'wifi', icon: '📶', name: 'Wi-Fi Share', desc: 'Share Wi-Fi credentials via QR code without revealing password.' },
  { id: 'ocr', icon: '🔍', name: 'OCR', desc: 'Extract text from images and screen captures on-device.' },
  { id: 'color', icon: '🎨', name: 'Color Picker', desc: 'Pick colors from anywhere on screen with hex/RGB values.' },
  { id: 'converter', icon: '📐', name: 'Unit Converter', desc: 'Convert between units — length, weight, temperature, currency.' },
  { id: 'timer', icon: '⏱️', name: 'Timer', desc: 'Floating countdown timer with multiple simultaneous timers.' },
  { id: 'battery', icon: '🔋', name: 'Battery', desc: 'Real-time battery status, temperature, and health monitoring.' },
  { id: 'drawer', icon: '🗂️', name: 'Tool Drawer', desc: 'Customizable floating dock — show only the tools you use.' },
]

export const specs = [
  { label: 'Latest Version', value: 'v5.2.2' },
  { label: 'Min SDK', value: 'Android 7.0 (API 24)' },
  { label: 'Target SDK', value: 'Android 16 (API 36)' },
  { label: 'Architecture', value: 'MVVM + Hilt DI + Jetpack Compose' },
  { label: 'Language', value: 'Kotlin' },
  { label: 'License', value: 'PocketOps Custom Open Source Fork License' },
  { label: 'APK Size', value: '~27.9 MB (universal), ~18.5 MB (ARM64)' },
  { label: 'Permissions', value: 'Overlay (SYSTEM_ALERT_WINDOW) — no internet required' },
  { label: 'Page Size', value: '16 KB page aligned (Android 16 ready)' },
  { label: 'Testing', value: 'JUnit 5 + MockK + Turbine + Room + Truth' },
]

export const themes = [
  { name: 'Ocean', colors: { primary: '#1976d2', surface: '#e3f2fd' } },
  { name: 'Forest', colors: { primary: '#2e7d32', surface: '#e8f5e9' } },
  { name: 'Sunset', colors: { primary: '#e65100', surface: '#fff3e0' } },
  { name: 'Lavender', colors: { primary: '#7b1fa2', surface: '#f3e5f5' } },
  { name: 'Ruby', colors: { primary: '#c62828', surface: '#ffebee' } },
  { name: 'Teal', colors: { primary: '#00695c', surface: '#e0f2f1' } },
  { name: 'Slate', colors: { primary: '#37474f', surface: '#eceff1' } },
  { name: 'Amber', colors: { primary: '#f57f17', surface: '#fff8e1' } },
]

export const testimonials = [
  { name: 'Alex Chen', role: 'Android Power User', text: 'QuickDash has completely replaced my old floating widget setup. The UPI QR scanner is a lifesaver.', device: 'Samsung Galaxy S24' },
  { name: 'Priya Sharma', role: 'Software Engineer', text: 'The on-device OCR and translator are incredibly fast. No ads, no tracking — exactly what I wanted.', device: 'Pixel 8 Pro' },
  { name: 'Marcus Johnson', role: 'Productivity Nerd', text: 'I use the floating calculator and notes every single day. The Material Design 3 theming is gorgeous.', device: 'OnePlus 12' },
  { name: 'Elena Rodriguez', role: 'Digital Minimalist', text: 'Finally, a tool that respects privacy. Zero network permissions and it still does everything I need.', device: 'Fairphone 5' },
]

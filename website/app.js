document.addEventListener('DOMContentLoaded', () => {

  // ==========================================
  // 1. Phone Clock Updater
  // ==========================================
  const clockElement = document.getElementById('phone-clock');
  const updateClock = () => {
    const now = new Date();
    let hours = now.getHours();
    let minutes = now.getMinutes();
    hours = hours < 10 ? '0' + hours : hours;
    minutes = minutes < 10 ? '0' + minutes : minutes;
    if (clockElement) {
      clockElement.textContent = `${hours}:${minutes}`;
    }
  };
  setInterval(updateClock, 1000);
  updateClock();


  // ==========================================
  // 2. 3D Parallax Tilt Effect
  // ==========================================
  const phoneContainer = document.getElementById('simulator-section');
  const phoneMockup = document.querySelector('.phone-mockup');

  if (phoneContainer && phoneMockup) {
    phoneContainer.addEventListener('mousemove', (e) => {
      const rect = phoneContainer.getBoundingClientRect();
      const x = e.clientX - rect.left - rect.width / 2;
      const y = e.clientY - rect.top - rect.height / 2;

      const rotX = -(y / rect.height) * 20;
      const rotY = (x / rect.width) * 20;

      phoneMockup.style.transform = `rotateX(${rotX}deg) rotateY(${rotY}deg)`;
    });

    phoneContainer.addEventListener('mouseleave', () => {
      phoneMockup.style.transform = 'rotateX(0deg) rotateY(0deg)';
    });
  }


  // ==========================================
  // 3. Draggable Floating Bubble
  // ==========================================
  const bubble = document.getElementById('floating-bubble');
  const phoneScreen = document.getElementById('phone-screen');
  const dashboardOverlay = document.getElementById('dashboard-overlay');
  const phoneHome = document.getElementById('phone-home');
  const toastElement = document.getElementById('sim-toast');

  let isDragging = false;
  let dragStartX = 0;
  let dragStartY = 0;
  let bubbleStartX = 0;
  let bubbleStartY = 0;
  let hasMovedSignificant = false;

  const showToast = (message) => {
    if (toastElement) {
      toastElement.textContent = message;
      toastElement.classList.add('show');
      setTimeout(() => {
        toastElement.classList.remove('show');
      }, 2000);
    }
  };

  const getEventCoordinates = (e) => {
    if (e.touches && e.touches.length > 0) {
      return { x: e.touches[0].clientX, y: e.touches[0].clientY };
    }
    return { x: e.clientX, y: e.clientY };
  };

  const onDragStart = (e) => {
    if (dashboardOverlay.classList.contains('active')) return;
    if (biometricLockScreen.classList.contains('active')) return;

    isDragging = true;
    hasMovedSignificant = false;
    bubble.classList.add('dragging');

    const coords = getEventCoordinates(e);
    dragStartX = coords.x;
    dragStartY = coords.y;

    const rect = bubble.getBoundingClientRect();
    const screenRect = phoneScreen.getBoundingClientRect();

    bubbleStartX = rect.left - screenRect.left;
    bubbleStartY = rect.top - screenRect.top;

    bubble.style.right = 'auto';

    document.addEventListener('mousemove', onDragMove, { passive: false });
    document.addEventListener('mouseup', onDragEnd);
    document.addEventListener('touchmove', onDragMove, { passive: false });
    document.addEventListener('touchend', onDragEnd);

    if (e.cancelable) e.preventDefault();
  };

  const onDragMove = (e) => {
    if (!isDragging) return;

    const coords = getEventCoordinates(e);
    const deltaX = coords.x - dragStartX;
    const deltaY = coords.y - dragStartY;

    if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
      hasMovedSignificant = true;
    }

    let nextX = bubbleStartX + deltaX;
    let nextY = bubbleStartY + deltaY;

    const maxLeft = 320 - 56;
    const maxTop = 640 - 56 - 15;
    const minTop = 36;

    nextX = Math.max(0, Math.min(nextX, maxLeft));
    nextY = Math.max(minTop, Math.min(nextY, maxTop));

    bubble.style.left = `${nextX}px`;
    bubble.style.top = `${nextY}px`;

    if (e.cancelable) e.preventDefault();
  };

  const onDragEnd = () => {
    if (!isDragging) return;
    isDragging = false;
    bubble.classList.remove('dragging');

    document.removeEventListener('mousemove', onDragMove);
    document.removeEventListener('mouseup', onDragEnd);
    document.removeEventListener('touchmove', onDragMove);
    document.removeEventListener('touchend', onDragEnd);

    if (!hasMovedSignificant) {
      openDashboard();
    }
  };

  bubble.addEventListener('mousedown', onDragStart);
  bubble.addEventListener('touchstart', onDragStart, { passive: false });

  const openDashboard = () => {
    if (biometricLockScreen.classList.contains('active')) return;
    dashboardOverlay.classList.add('active');
    phoneHome.style.opacity = '0.3';
  };

  const closeDashboard = () => {
    dashboardOverlay.classList.remove('active');
    phoneHome.style.opacity = '1';
    showSubview('view-main');
  };

  document.getElementById('btn-close-dash').addEventListener('click', closeDashboard);


  // ==========================================
  // 4. Subview Controllers & Nav Browser
  // ==========================================
  const viewMain = document.getElementById('view-main');
  const subviews = document.querySelectorAll('.subview-container:not(#view-main)');
  const toolItems = document.querySelectorAll('.tool-item');
  const backButtons = document.querySelectorAll('.subview-back-btn');

  const showSubview = (targetId) => {
    viewMain.classList.remove('active');
    subviews.forEach(view => view.classList.remove('active'));

    const targetView = document.getElementById(targetId);
    if (targetView) {
      targetView.classList.add('active');
      if (targetId === 'view-payments') {
        generatePaymentQr();
      } else if (targetId === 'view-wifi') {
        generateWifiQr();
      }
    }
  };

  toolItems.forEach(item => {
    item.addEventListener('click', () => {
      const target = item.getAttribute('data-target');
      if (target) {
        showSubview(target);
      }
    });
  });

  backButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      showSubview('view-main');
    });
  });

  // Traffic Toast Trigger
  document.getElementById('btn-toast-speed').addEventListener('click', () => {
    showToast('📊 Speed: 145 KB/s | Local DB Cleaned');
  });


  // ==========================================
  // 5. Notes Simulator Logic
  // ==========================================
  const noteInput = document.getElementById('note-input');
  const btnAddNote = document.getElementById('btn-add-note');
  const notesContainer = document.getElementById('notes-container');

  const createNoteElement = (text, isPinned = false) => {
    const item = document.createElement('div');
    item.className = `sim-note-item ${isPinned ? 'pinned' : ''}`;
    
    const noteText = document.createElement('span');
    noteText.className = 'sim-note-text';
    noteText.textContent = text;
    
    const actions = document.createElement('div');
    actions.className = 'sim-note-actions';
    
    const pinBtn = document.createElement('button');
    pinBtn.className = 'sim-note-btn pin-btn';
    pinBtn.innerHTML = '<span class="material-symbols-outlined" style="font-size: 0.8rem;">push_pin</span>';
    
    pinBtn.addEventListener('click', () => {
      item.classList.toggle('pinned');
      sortNotes();
      showToast(item.classList.contains('pinned') ? 'Note pinned' : 'Note unpinned');
    });
    
    actions.appendChild(pinBtn);
    item.appendChild(noteText);
    item.appendChild(actions);
    
    return item;
  };

  const sortNotes = () => {
    const notes = Array.from(notesContainer.children);
    notes.sort((a, b) => {
      const aPinned = a.classList.contains('pinned') ? 1 : 0;
      const bPinned = b.classList.contains('pinned') ? 1 : 0;
      return bPinned - aPinned;
    });
    notesContainer.innerHTML = '';
    notes.forEach(note => notesContainer.appendChild(note));
  };

  btnAddNote.addEventListener('click', () => {
    const text = noteInput.value.trim();
    if (text) {
      const note = createNoteElement(text);
      notesContainer.appendChild(note);
      noteInput.value = '';
      sortNotes();
      showToast('Note saved to SQLite Room');
    }
  });

  noteInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') btnAddNote.click();
  });


  // ==========================================
  // 6. Clipboard View
  // ==========================================
  document.querySelectorAll('.btn-copy-clip').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const item = e.target.closest('.sim-clip-item');
      const text = item.querySelector('.sim-clip-text').textContent;
      
      navigator.clipboard.writeText(text).then(() => {
        showToast('Copied to system clipboard!');
      }).catch(() => {
        showToast('Clipboard copy simulated!');
      });
    });
  });


  // ==========================================
  // 7. Dynamic QR Generator Canvas Logic
  // ==========================================
  const drawQRCodeOnCanvas = (canvasId, text, color) => {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const size = canvas.width;
    
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, size, size);

    let hash = 0;
    for (let i = 0; i < text.length; i++) {
      hash = text.charCodeAt(i) + ((hash << 5) - hash);
    }

    const gridCells = 21; 
    const cellSize = size / gridCells;

    ctx.fillStyle = '#000000';

    const drawFinderPattern = (x, y) => {
      ctx.fillStyle = '#000000';
      ctx.fillRect(x * cellSize, y * cellSize, 7 * cellSize, 7 * cellSize);
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect((x + 1) * cellSize, (y + 1) * cellSize, 5 * cellSize, 5 * cellSize);
      ctx.fillStyle = color || '#000000';
      ctx.fillRect((x + 2) * cellSize, (y + 2) * cellSize, 3 * cellSize, 3 * cellSize);
    };

    drawFinderPattern(0, 0);
    drawFinderPattern(14, 0);
    drawFinderPattern(0, 14);

    ctx.fillStyle = color || '#000000';
    ctx.fillRect(14 * cellSize, 14 * cellSize, 2 * cellSize, 2 * cellSize);

    for (let row = 0; row < gridCells; row++) {
      for (let col = 0; col < gridCells; col++) {
        if ((row < 8 && col < 8) || (row < 8 && col >= 13) || (row >= 13 && col < 8)) {
          continue;
        }

        const val = Math.abs(Math.sin(hash + (row * 13) + (col * 37)));
        if (val > 0.45) {
          ctx.fillStyle = (row + col) % 5 === 0 ? color || '#000000' : '#000000';
          ctx.fillRect(col * cellSize, row * cellSize, Math.ceil(cellSize), Math.ceil(cellSize));
        }
      }
    }
  };

  // Payment Switcher
  const tabUpi = document.getElementById('tab-upi');
  const tabPaypal = document.getElementById('tab-paypal');
  const paymentLabel = document.getElementById('payment-label');
  const paymentTarget = document.getElementById('payment-target');
  let activePaymentMode = 'upi';

  const generatePaymentQr = () => {
    const accentColor = getComputedStyle(document.documentElement).getPropertyValue('--bg-accent').trim();
    if (activePaymentMode === 'upi') {
      paymentLabel.textContent = 'UPI Merchant Target';
      paymentTarget.textContent = 'upi://pay?pa=balaji@upi&am=100';
      drawQRCodeOnCanvas('payment-qr-canvas', 'upi://pay?pa=balaji@upi&am=100', accentColor);
    } else {
      paymentLabel.textContent = 'PayPal Payment Link';
      paymentTarget.textContent = 'https://paypal.me/balajitech/10usd';
      drawQRCodeOnCanvas('payment-qr-canvas', 'https://paypal.me/balajitech/10usd', accentColor);
    }
  };

  tabUpi.addEventListener('click', () => {
    activePaymentMode = 'upi';
    tabUpi.classList.add('active');
    tabPaypal.classList.remove('active');
    generatePaymentQr();
    showToast('Flipped target to UPI');
  });

  tabPaypal.addEventListener('click', () => {
    activePaymentMode = 'paypal';
    tabUpi.classList.remove('active');
    tabPaypal.classList.add('active');
    generatePaymentQr();
    showToast('Flipped target to PayPal');
  });


  // Wi-Fi QR Generator
  const wifiSSID = document.getElementById('wifi-ssid');
  const wifiPass = document.getElementById('wifi-pass');
  const wifiSecurity = document.getElementById('wifi-security');
  const btnGenerateWifi = document.getElementById('btn-generate-wifi-qr');
  const wifiQrBox = document.getElementById('wifi-qr-box');
  const wifiQrString = document.getElementById('wifi-qr-string');

  const generateWifiQr = () => {
    const ssid = wifiSSID.value.trim() || 'QuickDash_HQ';
    const pass = wifiPass.value.trim() || 'flywithbubble';
    const security = wifiSecurity.value;
    const accentColor = getComputedStyle(document.documentElement).getPropertyValue('--bg-accent').trim();

    const qrText = `WIFI:S:${ssid};T:${security};P:${pass};;`;
    
    wifiQrString.textContent = qrText;
    wifiQrBox.style.display = 'block';

    drawQRCodeOnCanvas('wifi-qr-canvas', qrText, accentColor);
    showToast('Wi-Fi QR Code Drawn');
  };

  btnGenerateWifi.addEventListener('click', generateWifiQr);


  // ==========================================
  // 8. Frictionless Messaging
  // ==========================================
  const chatPlatform = document.getElementById('chat-platform');
  const chatNumber = document.getElementById('chat-number');
  const chatTemplate = document.getElementById('chat-template');
  const btnLaunchChat = document.getElementById('btn-launch-chat');

  chatPlatform.addEventListener('change', () => {
    const platform = chatPlatform.value;
    btnLaunchChat.innerHTML = `<span class="material-symbols-outlined" style="font-size: 1rem;">send</span> Launch ${platform}`;
    if (platform === 'WhatsApp') {
      btnLaunchChat.style.backgroundColor = '#25D366';
    } else if (platform === 'Telegram') {
      btnLaunchChat.style.backgroundColor = '#0088cc';
    } else {
      btnLaunchChat.style.backgroundColor = '#8b5cf6';
    }
  });

  btnLaunchChat.addEventListener('click', () => {
    const platform = chatPlatform.value;
    const num = chatNumber.value.trim() || '+1234567890';
    const text = encodeURIComponent(chatTemplate.value);
    showToast(`Opening ${platform} thread...`);
    
    let url = '';
    if (platform === 'WhatsApp') {
      url = `https://wa.me/${num}?text=${text}`;
    } else if (platform === 'Telegram') {
      url = `https://t.me/${num}`;
    } else {
      url = `https://signal.me/#p/${num}`;
    }
    
    setTimeout(() => {
      window.open(url, '_blank');
    }, 800);
  });


  // ==========================================
  // 9. Floating Browser View & Navigable results
  // ==========================================
  const browserSearch = document.getElementById('browser-search');
  const btnBrowserGo = document.getElementById('btn-browser-go');
  const browserViewport = document.getElementById('browser-viewport');

  const showBrowserPage = (pageName) => {
    showToast(`Loading: ${pageName}`);
    if (pageName === 'docs') {
      browserViewport.innerHTML = `
        <div style="font-size:0.6rem; color:#333;">
          <div style="font-weight:700; border-bottom:1px solid #ddd; padding-bottom:3px; margin-bottom:6px; display:flex; justify-content:space-between;">
            <span>📖 Material You Guidelines</span>
            <span id="btn-browser-back" style="color:var(--bg-accent); cursor:pointer; font-weight:700;">Back</span>
          </div>
          <p style="margin:0 0 6px; color:#555; line-height:1.25;">Dynamic color systems derive user-facing themes from visual wallpaper configurations. The Android Monet engine creates fine HSL shades automatically.</p>
          <a href="#" id="link-browser-git" style="color:blue; text-decoration:underline;">View repo docs</a>
        </div>
      `;
      // Attach local listeners
      document.getElementById('btn-browser-back').addEventListener('click', executeBrowserSearch);
      document.getElementById('link-browser-git').addEventListener('click', (e) => {
        e.preventDefault();
        showBrowserPage('github');
      });
    } else if (pageName === 'github') {
      browserViewport.innerHTML = `
        <div style="font-size:0.6rem; color:#333;">
          <div style="font-weight:700; border-bottom:1px solid #ddd; padding-bottom:3px; margin-bottom:6px; display:flex; justify-content:space-between;">
            <span>🐙 BalajiTechLabs / quickdash</span>
            <span id="btn-browser-back" style="color:var(--bg-accent); cursor:pointer; font-weight:700;">Back</span>
          </div>
          <div style="font-weight:600; color:#111;">v4.4.0 (Latest Tag)</div>
          <p style="margin:4px 0; color:#444;">Core floating UI package with Material 3 biometric security options, persistency, and notifications shade listeners.</p>
        </div>
      `;
      document.getElementById('btn-browser-back').addEventListener('click', () => showBrowserPage('docs'));
    }
  };

  const executeBrowserSearch = () => {
    const query = browserSearch.value.trim();
    if (query) {
      showToast(`Searching floating browser...`);
      browserViewport.innerHTML = `
        <div class="sim-browser-result-title" style="cursor:pointer; text-decoration:underline;" id="search-result-title">Material Design 3 - Android Developers</div>
        <div class="sim-browser-result-url">developer.android.com/design/ui/m3</div>
        <div class="sim-browser-result-desc">Click link to verify page navigation within the overlay viewport without leaving home applications!</div>
      `;
      
      document.getElementById('search-result-title').addEventListener('click', () => {
        showBrowserPage('docs');
      });
    }
  };

  btnBrowserGo.addEventListener('click', executeBrowserSearch);
  browserSearch.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') executeBrowserSearch();
  });


  // ==========================================
  // 10. Interactive Diagnostic Terminal & Exporter
  // ==========================================
  const terminalScreen = document.getElementById('terminal-screen');
  const terminalInput = document.getElementById('terminal-input');
  const btnExportLogs = document.getElementById('btn-export-logs');
  let currentCrashLogText = '';

  const logToTerminal = (text, type = 'log') => {
    const logDiv = document.createElement('div');
    if (type === 'error') {
      logDiv.style.color = '#EF4444';
      logDiv.style.textShadow = '0 0 2px rgba(239,68,68,0.5)';
    } else if (type === 'warning') {
      logDiv.style.color = '#F59E0B';
      logDiv.style.textShadow = '0 0 2px rgba(245,158,11,0.5)';
    } else if (type === 'system') {
      logDiv.style.color = '#3B82F6';
      logDiv.style.textShadow = '0 0 2px rgba(59,130,246,0.5)';
    }
    logDiv.textContent = text;
    terminalScreen.appendChild(logDiv);
    terminalScreen.scrollTop = terminalScreen.scrollHeight;
    
    // Accumulate log text for exporter
    currentCrashLogText += `${type.toUpperCase()}: ${text}\n`;
  };

  terminalInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      const command = terminalInput.value.trim();
      terminalInput.value = '';
      if (!command) return;

      logToTerminal(`$ ${command}`, 'system');

      const parts = command.split(' ');
      const mainCmd = parts[0].toLowerCase();

      setTimeout(() => {
        switch (mainCmd) {
          case '/help':
            logToTerminal('Available commands:');
            logToTerminal('  /logs   - Show recent Diagnostic Intercept logs');
            logToTerminal('  /crash  - Simulate a null pointer Java crash logger trace');
            logToTerminal('  /stats  - Query active background workers memory usage');
            logToTerminal('  /clear  - Wipe logs from terminal screen');
            break;
          case '/clear':
            terminalScreen.innerHTML = '<div>[Terminal Cleared] Monitor active.</div>';
            currentCrashLogText = '';
            break;
          case '/stats':
            logToTerminal('--- QuickDash Telemetry Statistics ---');
            logToTerminal('Uptime: 2h 45m 12s');
            logToTerminal('SQLite note count: 2 items active');
            logToTerminal('DataStore memory cache: 14 keys mapped');
            logToTerminal('Active background tasks: 1 (Telegram Tracker)');
            logToTerminal('Heap size: 84.6 MB | Free memory: 18.2 MB');
            break;
          case '/logs':
            logToTerminal('[2026-07-14 00:42:01] [AppContainer] Initializing Room DB...', 'log');
            logToTerminal('[2026-07-14 00:42:02] [Firebase] Tracker listener active', 'log');
            logToTerminal('[2026-07-14 00:42:03] [UserStore] Preferences loaded from config', 'log');
            logToTerminal('[2026-07-14 00:42:15] [DiagLog] Exception handler attached', 'log');
            logToTerminal('[2026-07-14 00:45:30] [Clipboard] Copied item detected', 'warning');
            break;
          case '/crash':
            logToTerminal('FATAL EXCEPTION: main', 'error');
            logToTerminal('java.lang.NullPointerException: Attempt to read field on a null object reference', 'error');
            logToTerminal('  at com.balajitechlabs.quickdash.features.notes.QuickNotesScreenKt.QuickNotesScreen(QuickNotesScreen.kt:85)', 'error');
            logToTerminal('  at com.balajitechlabs.quickdash.core.ui.QuickDashAppKt.FloatingContent(QuickDashApp.kt:142)', 'error');
            logToTerminal('  at com.balajitechlabs.quickdash.core.services.FloatingBubbleService.onCreate(FloatingBubbleService.kt:56)', 'error');
            logToTerminal('[DiagnosticLogger] CRASH CAPTURED! Intercepting trace and logging telemetry...', 'warning');
            break;
          default:
            logToTerminal(`Command not recognized: "${mainCmd}". Type /help for assistance.`, 'error');
        }
      }, 200);
    }
  });

  // Client-side Log Exporter Generator
  btnExportLogs.addEventListener('click', () => {
    if (!currentCrashLogText) {
      currentCrashLogText = "SYSTEM_LOG: Diagnostic logs empty. Run /logs or /crash first.\n";
    }
    
    const telemetryHeader = `--------------------------------------------------\n` +
                            `QUICKDASH SYSTEM DIAGNOSTIC REPORT\n` +
                            `Generated: ${new Date().toISOString()}\n` +
                            `Device: Mock Android Simulator overlays\n` +
                            `OS Version: macOS client dev simulation\n` +
                            `--------------------------------------------------\n\n`;
                            
    const finalBlobText = telemetryHeader + currentCrashLogText;
    const blob = new Blob([finalBlobText], { type: 'text/plain' });
    const blobURL = URL.createObjectURL(blob);
    
    const downloadLink = document.createElement('a');
    downloadLink.href = blobURL;
    downloadLink.download = `quickdash_crash_report_${Date.now()}.log`;
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
    
    showToast('Downloaded quickdash_crash_report.log');
  });


  // ==========================================
  // 11. Draggable Notification Shade shade control
  // ==========================================
  const notificationShade = document.getElementById('notification-shade');
  const notificationHandle = document.getElementById('notification-handle');
  const notificationsList = document.getElementById('shade-notifications-list');
  const btnClearNotifications = document.getElementById('btn-clear-notifications');

  let isShadeDragging = false;
  let shadeStartY = 0;
  let shadeStartTranslate = -510;
  let shadeCurrentTranslate = -510;

  const onShadeDragStart = (e) => {
    isShadeDragging = true;
    notificationShade.classList.add('dragging-shade');
    const coords = getEventCoordinates(e);
    shadeStartY = coords.y;

    document.addEventListener('mousemove', onShadeDragMove, { passive: false });
    document.addEventListener('mouseup', onShadeDragEnd);
    document.addEventListener('touchmove', onShadeDragMove, { passive: false });
    document.addEventListener('touchend', onShadeDragEnd);

    if (e.cancelable) e.preventDefault();
  };

  const onShadeDragMove = (e) => {
    if (!isShadeDragging) return;
    const coords = getEventCoordinates(e);
    const deltaY = coords.y - shadeStartY;
    
    shadeCurrentTranslate = shadeStartTranslate + deltaY;
    // Bounds: closed (-510px) to fully open (0px)
    shadeCurrentTranslate = Math.max(-510, Math.min(shadeCurrentTranslate, 0));
    
    notificationShade.style.transform = `translateY(${shadeCurrentTranslate + 510}px)`;

    if (e.cancelable) e.preventDefault();
  };

  const onShadeDragEnd = () => {
    if (!isShadeDragging) return;
    isShadeDragging = false;
    notificationShade.classList.remove('dragging-shade');

    document.removeEventListener('mousemove', onShadeDragMove);
    document.removeEventListener('mouseup', onShadeDragEnd);
    document.removeEventListener('touchmove', onShadeDragMove);
    document.removeEventListener('touchend', onShadeDragEnd);

    // Pull threshold: if dragged past halfway open (-255px), snap fully open
    if (shadeCurrentTranslate > -255) {
      notificationShade.style.transform = 'translateY(510px)';
      shadeStartTranslate = 0;
      shadeCurrentTranslate = 0;
      showToast('Notification Shade Open');
    } else {
      notificationShade.style.transform = 'translateY(0px)';
      shadeStartTranslate = -510;
      shadeCurrentTranslate = -510;
    }
  };

  notificationHandle.addEventListener('mousedown', onShadeDragStart);
  notificationHandle.addEventListener('touchstart', onShadeDragStart, { passive: false });

  // Swipe away / click-to-clear notification items
  notificationsList.addEventListener('click', (e) => {
    const item = e.target.closest('.shade-item');
    if (item) {
      item.style.transform = 'translateX(350px)';
      item.style.opacity = '0';
      setTimeout(() => {
        item.remove();
        showToast('Notification cleared');
        if (notificationsList.children.length === 0) {
          notificationsList.innerHTML = '<div style="font-size:0.65rem; text-align:center; padding: 20px; color:var(--text-secondary);">No incoming notifications</div>';
        }
      }, 200);
    }
  });

  btnClearNotifications.addEventListener('click', () => {
    notificationsList.innerHTML = '<div style="font-size:0.65rem; text-align:center; padding: 20px; color:var(--text-secondary);">No incoming notifications</div>';
    showToast('Cleared all notifications');
  });


  // ==========================================
  // 12. Biometric Fingerprint Lock Logic
  // ==========================================
  const biometricLockScreen = document.getElementById('biometric-lock-screen');
  const fingerprintSensor = document.getElementById('fingerprint-sensor');
  const scanProgressFill = document.getElementById('scan-progress');
  const scanInstructions = document.getElementById('scan-instructions');
  const btnLockApp = document.getElementById('btn-lock-app');

  let holdInterval = null;
  let scanProgressVal = 0;

  const triggerLockScreen = () => {
    closeDashboard();
    biometricLockScreen.classList.add('active');
    scanProgressVal = 0;
    scanProgressFill.style.width = '0%';
    scanInstructions.textContent = 'Press & Hold Fingerprint Sensor';
  };

  btnLockApp.addEventListener('click', triggerLockScreen);

  const startScanning = (e) => {
    if (holdInterval) return;
    fingerprintSensor.classList.add('scanning');
    scanInstructions.textContent = 'Authenticating fingerprint...';
    
    holdInterval = setInterval(() => {
      scanProgressVal += 4;
      scanProgressFill.style.width = `${Math.min(scanProgressVal, 100)}%`;
      
      if (scanProgressVal >= 100) {
        clearInterval(holdInterval);
        holdInterval = null;
        fingerprintSensor.classList.remove('scanning');
        biometricLockScreen.classList.remove('active');
        showToast('Biometrics Clear: Hub Unlocked');
        openDashboard();
      }
    }, 50);

    if (e.cancelable) e.preventDefault();
  };

  const stopScanning = () => {
    if (holdInterval) {
      clearInterval(holdInterval);
      holdInterval = null;
      scanProgressVal = 0;
      scanProgressFill.style.width = '0%';
      fingerprintSensor.classList.remove('scanning');
      scanInstructions.textContent = 'Verify failed: Sensor release detected';
      showToast('Validation failed');
    }
  };

  fingerprintSensor.addEventListener('mousedown', startScanning);
  fingerprintSensor.addEventListener('touchstart', startScanning, { passive: false });
  window.addEventListener('mouseup', stopScanning);
  window.addEventListener('touchend', stopScanning);


  // ==========================================
  // 13. Dynamic Monet Theme & Wallpaper Color Extractor
  // ==========================================
  const themeButtons = document.querySelectorAll('.theme-select-btn');
  const fontButtons = document.querySelectorAll('.font-select-btn');
  const colorPicker = document.getElementById('monet-color-picker');
  const hexValSpan = document.getElementById('monet-hex-val');
  const wallpaperThumbs = document.querySelectorAll('.wallpaper-thumb:not(#wallpaper-upload-btn)');
  const wallpaperUploadBtn = document.getElementById('wallpaper-upload-btn');
  const wallpaperFileInput = document.getElementById('wallpaper-file-input');

  const hexToHSL = (hex) => {
    let r = parseInt(hex.slice(1, 3), 16) / 255;
    let g = parseInt(hex.slice(3, 5), 16) / 255;
    let b = parseInt(hex.slice(5, 7), 16) / 255;

    let max = Math.max(r, g, b), min = Math.min(r, g, b);
    let h, s, l = (max + min) / 2;

    if (max === min) {
      h = s = 0; 
    } else {
      let d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      switch (max) {
        case r: h = (g - b) / d + (g < b ? 6 : 0); break;
        case g: h = (b - r) / d + 2; break;
        case b: h = (r - g) / d + 4; break;
      }
      h /= 6;
    }

    return {
      h: Math.round(h * 360),
      s: Math.round(s * 100),
      l: Math.round(l * 100)
    };
  };

  const applyCustomMonetTheme = (hex) => {
    const hsl = hexToHSL(hex);
    const root = document.documentElement;
    root.style.setProperty('--bg-primary', `hsl(${hsl.h}, ${Math.min(hsl.s, 40)}%, 7%)`);
    root.style.setProperty('--bg-secondary', `hsl(${hsl.h}, ${Math.min(hsl.s, 40)}%, 11%)`);
    root.style.setProperty('--bg-accent', `hsl(${hsl.h}, ${hsl.s}%, 55%)`);
    root.style.setProperty('--bg-accent-rgb', `${hsl.h}, ${hsl.s}%, 55%`);
    root.style.setProperty('--surface', `hsla(${hsl.h}, ${Math.min(hsl.s, 30)}%, 12%, 0.6)`);
    root.style.setProperty('--border-color', `hsla(${hsl.h}, ${hsl.s}%, 55%, 0.2)`);
    root.style.setProperty('--on-primary', `hsl(${hsl.h}, ${hsl.s}%, 5%)`);
    root.style.setProperty('--text-primary', `hsl(${hsl.h}, 10%, 94%)`);
    root.style.setProperty('--text-secondary', `hsl(${hsl.h}, 20%, 72%)`);
    root.style.setProperty('--glow-color', `hsla(${hsl.h}, ${hsl.s}%, 55%, 0.35)`);

    if (bubble) bubble.style.borderColor = `hsl(${hsl.h}, ${hsl.s}%, 55%)`;

    generatePaymentQr();
    if (wifiQrBox.style.display !== 'none') {
      generateWifiQr();
    }
  };

  // Color extraction from wallpaper source helper
  const extractDominantColor = (imgSrc, callback) => {
    const img = new Image();
    img.crossOrigin = 'Anonymous';
    img.onload = () => {
      // Create hidden offscreen canvas to sample pixels
      const canvas = document.createElement('canvas');
      canvas.width = 10;
      canvas.height = 10;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0, 10, 10);
      
      // Fetch central sample pixel to represent dominant hue
      const pixelData = ctx.getImageData(5, 5, 1, 1).data;
      
      // Convert RGB to HEX
      const rgbToHex = (r, g, b) => '#' + [r, g, b].map(x => {
        const hex = x.toString(16);
        return hex.length === 1 ? '0' + hex : hex;
      }).join('');

      const extractedHex = rgbToHex(pixelData[0], pixelData[1], pixelData[2]);
      callback(extractedHex);
    };
    img.src = imgSrc;
  };

  // Set Wallpaper Preset click events
  wallpaperThumbs.forEach(thumb => {
    thumb.addEventListener('click', () => {
      wallpaperThumbs.forEach(t => t.classList.remove('active'));
      wallpaperUploadBtn.classList.remove('active');
      thumb.classList.add('active');

      const bgType = thumb.getAttribute('data-bg');
      const screen = document.getElementById('phone-screen');
      screen.className = 'phone-screen'; // reset
      screen.style.backgroundImage = ''; // reset uploaded image override

      let mockDominantHex = '#FE9F06';

      if (bgType === 'gradient-1') {
        screen.classList.add('wallpaper-1');
        mockDominantHex = '#4c1d95'; // Midnight Purple dominant hex
      } else if (bgType === 'gradient-2') {
        screen.classList.add('wallpaper-2');
        mockDominantHex = '#9d174d'; // Crimson Core dominant hex
      } else {
        screen.classList.add('wallpaper-3');
        mockDominantHex = '#0f766e'; // Ocean Teal dominant hex
      }

      themeButtons.forEach(b => b.classList.remove('active'));
      document.documentElement.removeAttribute('data-theme');
      
      // Sync color picker UI
      colorPicker.value = mockDominantHex;
      hexValSpan.textContent = mockDominantHex.toUpperCase();
      hexValSpan.style.color = mockDominantHex;

      applyCustomMonetTheme(mockDominantHex);
      showToast('Material You: Wallpaper colors extracted');
    });
  });

  // Upload Custom Wallpaper trigger
  wallpaperUploadBtn.addEventListener('click', () => {
    wallpaperFileInput.click();
  });

  wallpaperFileInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        const dataURL = event.target.result;
        
        // Update screen background image
        const screen = document.getElementById('phone-screen');
        screen.className = 'phone-screen';
        screen.style.backgroundImage = `url(${dataURL})`;

        wallpaperThumbs.forEach(t => t.classList.remove('active'));
        wallpaperUploadBtn.classList.add('active');

        // Extract dominant color from image pixels
        extractDominantColor(dataURL, (extractedHex) => {
          themeButtons.forEach(b => b.classList.remove('active'));
          document.documentElement.removeAttribute('data-theme');

          colorPicker.value = extractedHex;
          hexValSpan.textContent = extractedHex.toUpperCase();
          hexValSpan.style.color = extractedHex;

          applyCustomMonetTheme(extractedHex);
          showToast('Material You color extraction complete!');
        });
      };
      reader.readAsDataURL(file);
    }
  });

  // Color picker event listener
  if (colorPicker) {
    colorPicker.addEventListener('input', (e) => {
      const newHex = e.target.value;
      hexValSpan.textContent = newHex.toUpperCase();
      hexValSpan.style.color = newHex;
      
      themeButtons.forEach(b => b.classList.remove('active'));
      document.documentElement.removeAttribute('data-theme');
      
      applyCustomMonetTheme(newHex);
    });
  }

  // Theme Presets Buttons
  themeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const root = document.documentElement;
      root.style.removeProperty('--bg-primary');
      root.style.removeProperty('--bg-secondary');
      root.style.removeProperty('--bg-accent');
      root.style.removeProperty('--bg-accent-rgb');
      root.style.removeProperty('--surface');
      root.style.removeProperty('--border-color');
      root.style.removeProperty('--on-primary');
      root.style.removeProperty('--text-primary');
      root.style.removeProperty('--text-secondary');
      root.style.removeProperty('--glow-color');

      const themeName = btn.getAttribute('data-theme');
      root.setAttribute('data-theme', themeName);
      
      themeButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      if (colorPicker) colorPicker.value = '#FE9F06';
      if (hexValSpan) {
        hexValSpan.textContent = '#FE9F06';
        hexValSpan.style.color = 'var(--bg-accent)';
      }

      if (bubble) bubble.style.borderColor = '';

      generatePaymentQr();
      if (wifiQrBox.style.display !== 'none') {
        generateWifiQr();
      }
    });
  });

  // Fonts Selector Buttons
  fontButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const fontVal = btn.getAttribute('data-font');
      document.documentElement.style.setProperty('--font-family', fontVal);

      fontButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });


  // ==========================================
  // 14. Setup Info Tab Switching
  // ==========================================
  const setupTabs = document.querySelectorAll('.setup-tab-btn');
  const setupContents = document.querySelectorAll('.setup-tab-content');

  setupTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const targetTab = tab.getAttribute('data-tab');
      
      setupTabs.forEach(t => t.classList.remove('active'));
      setupContents.forEach(c => c.classList.remove('active'));
      
      tab.classList.add('active');
      document.getElementById(targetTab).classList.add('active');
    });
  });


  // ==========================================
  // 15. Scroll-driven Entry Reveal Animations
  // ==========================================
  const reveals = document.querySelectorAll('.reveal');
  const revealOnScroll = () => {
    reveals.forEach(el => {
      const windowHeight = window.innerHeight;
      const elementTop = el.getBoundingClientRect().top;
      const elementVisible = 120;
      if (elementTop < windowHeight - elementVisible) {
        el.classList.add('visible');
      } else {
        el.classList.remove('visible');
      }
    });
  };

  window.addEventListener('scroll', revealOnScroll);
  revealOnScroll();


  // ==========================================
  // 16. Video Preview Controller
  // ==========================================
  const btnPlayVideo = document.getElementById('btn-play-video');
  const videoAnimScreen = document.getElementById('video-anim-screen');
  const demoVideo = document.getElementById('demo-video');

  if (btnPlayVideo) {
    btnPlayVideo.addEventListener('click', () => {
      videoAnimScreen.style.display = 'none';
      demoVideo.style.display = 'block';
      
      demoVideo.load();
      demoVideo.play().catch(err => {
        console.log("Custom video.mp4 missing. Simulating workflow showcase.");
        videoAnimScreen.style.display = 'flex';
        demoVideo.style.display = 'none';
        
        showToast("Place demo inside website/assets/preview.mp4");
      });
    });
  }

});

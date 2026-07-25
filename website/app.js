/* ═══════════════════════════════════════════
   QuickDash v5.0.0 — app.js
   ═══════════════════════════════════════════ */

/* ─── Sticky header ─── */
const hdr = document.getElementById('site-header');
window.addEventListener('scroll', () => hdr.classList.toggle('scrolled', scrollY > 12), {passive:true});

/* ─── Live clock ─── */
function tick() {
  const n = new Date();
  const t = `${String(n.getHours()).padStart(2,'0')}:${String(n.getMinutes()).padStart(2,'0')}`;
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
  const mos  = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const d = `${days[n.getDay()]}, ${mos[n.getMonth()]} ${n.getDate()}`;
  ['p-time','p-clock'].forEach(id => { const el=document.getElementById(id); if(el) el.textContent=t; });
  const pd = document.getElementById('p-date'); if(pd) pd.textContent=d;
}
tick(); setInterval(tick, 10000);

/* ─── KeepAndroidOpen countdown ─── */
const END = new Date('2027-01-01T00:00:00Z').getTime();
function countdown() {
  const r = END - Date.now();
  if (r <= 0) return;
  const pad = n => String(n).padStart(2,'0');
  const el = id => document.getElementById(id);
  if(el('cd-d')) el('cd-d').textContent = Math.floor(r/86400000);
  if(el('cd-h')) el('cd-h').textContent = pad(Math.floor((r%86400000)/3600000));
  if(el('cd-m')) el('cd-m').textContent = pad(Math.floor((r%3600000)/60000));
  if(el('cd-s')) el('cd-s').textContent = pad(Math.floor((r%60000)/1000));
}
countdown(); setInterval(countdown, 1000);

/* ─── Scroll reveal ─── */
const ro = new IntersectionObserver(entries => {
  entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('in'); ro.unobserve(e.target); } });
}, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' });
document.querySelectorAll('.reveal').forEach(el => ro.observe(el));

/* ─── Bento card mouse glow ─── */
document.querySelectorAll('.bento-card').forEach(card => {
  card.addEventListener('mousemove', e => {
    const r = card.getBoundingClientRect();
    const x = ((e.clientX - r.left) / r.width * 100).toFixed(1);
    const y = ((e.clientY - r.top) / r.height * 100).toFixed(1);
    card.style.setProperty('--mx', x + '%');
    card.style.setProperty('--my', y + '%');
  });
});

/* ─── Toast utility ─── */
const toast = document.getElementById('sim-toast');
function showToast(msg, ms=1800) {
  if (!toast) return;
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), ms);
}

/* ─── Phone Simulator ─── */
const bubble  = document.getElementById('bubble');
const overlay = document.getElementById('dash-overlay');
const grid    = document.getElementById('tools-grid');
const detail  = document.getElementById('tool-detail');
const detBody = document.getElementById('detail-body');
const detTitle= document.getElementById('detail-title');
const btnClose= document.getElementById('btn-close');
const btnBack = document.getElementById('btn-back');
const btnCam  = document.getElementById('btn-cam');

const open = () => { overlay.classList.add('open'); grid.style.display='grid'; detail.style.display='none'; };
const close= () => overlay.classList.remove('open');

if (bubble)  bubble.addEventListener('click', open);
if (btnClose)btnClose.addEventListener('click', close);
if (overlay) overlay.addEventListener('click', e => { if(e.target===overlay) close(); });

if (btnCam) btnCam.addEventListener('click', () => {
  showToast('📸 Screenshot saved to Photos!');
  btnCam.style.transform='scale(0.8)';
  setTimeout(()=>btnCam.style.transform='', 200);
});

if (btnBack) btnBack.addEventListener('click', () => {
  grid.style.display='grid';
  detail.style.display='none';
});

/* Tool content registry */
const TOOLS = {
  collect: {
    title:'Quick Collect — QR Payments',
    html:`<div style="text-align:center;padding:8px 0;">
      <div style="background:linear-gradient(135deg,#3DDC84 0%,#00897B 100%);border-radius:18px;padding:22px;margin-bottom:14px;">
        <div style="width:80px;height:80px;background:#fff;border-radius:14px;margin:0 auto;display:flex;align-items:center;justify-content:center;">
          <span class="ms" style="font-size:2.6rem;color:#3DDC84;font-variation-settings:'FILL' 1;">qr_code_2</span>
        </div>
        <p style="color:#fff;font-weight:700;font-size:0.78rem;margin-top:10px;opacity:0.95;">UPI &amp; PayPal QR Generator</p>
      </div>
      <div style="display:flex;gap:6px;justify-content:center;flex-wrap:wrap;margin-bottom:12px;">
        <span style="background:#E8F5E9;color:#1B5E20;border:1px solid #C8E6C9;padding:5px 12px;border-radius:8px;font-size:0.7rem;font-weight:700;">₹ 100</span>
        <span style="background:#E8F5E9;color:#1B5E20;border:1px solid #C8E6C9;padding:5px 12px;border-radius:8px;font-size:0.7rem;font-weight:700;">₹ 500</span>
        <span style="background:#E8F5E9;color:#1B5E20;border:1px solid #C8E6C9;padding:5px 12px;border-radius:8px;font-size:0.7rem;font-weight:700;">₹ 1,000</span>
        <span style="background:#E8F5E9;color:#1B5E20;border:1px solid #C8E6C9;padding:5px 12px;border-radius:8px;font-size:0.7rem;font-weight:700;">Custom</span>
      </div>
      <p style="font-size:0.72rem;color:#6B7280;line-height:1.55;">Circular dot-pattern QR · Custom note field · Scan-to-pay</p>
    </div>`
  },
  chat: {
    title:'Quick Chat — Direct Messaging',
    html:`<div style="padding:4px 0;">
      <p style="font-size:0.72rem;color:#6B7280;margin-bottom:12px;line-height:1.5;">Launch a direct chat without saving the number as a contact.</p>
      ${[
        {c:'#25D366',n:'WhatsApp',i:'chat'},
        {c:'#0088CC',n:'Telegram',i:'send'},
        {c:'#3A76F0',n:'Signal',i:'security'},
        {c:'#007AFF',n:'iMessage',i:'message'},
      ].map(a=>`<div style="background:#F8F9FA;border:1px solid #E5E7EB;border-radius:10px;padding:11px 14px;display:flex;align-items:center;gap:10px;margin-bottom:7px;">
        <div style="width:32px;height:32px;border-radius:10px;background:${a.c};display:flex;align-items:center;justify-content:center;flex-shrink:0;">
          <span class="ms" style="font-size:0.95rem;color:#fff;font-variation-settings:'FILL' 1;">${a.i}</span>
        </div>
        <span style="font-size:0.8rem;font-weight:600;color:#1A1A2E;">${a.n} Direct</span>
        <span class="ms" style="margin-left:auto;color:#D1D5DB;font-size:0.9rem;">chevron_right</span>
      </div>`).join('')}
    </div>`
  },
  social: {
    title:'Quick Social — App Launchers',
    html:`<div style="padding:4px 0;">
      <p style="font-size:0.72rem;color:#6B7280;margin-bottom:12px;">Tap to open instantly.</p>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:10px;">
        ${[
          {c:'#E4405F',n:'Instagram',i:'photo_camera'},
          {c:'#1877F2',n:'Facebook',i:'public'},
          {c:'#000',n:'X',i:'close'},
          {c:'#0A66C2',n:'LinkedIn',i:'work'},
          {c:'#181717',n:'GitHub',i:'code'},
          {c:'#25D366',n:'WhatsApp',i:'chat'},
          {c:'#0088CC',n:'Telegram',i:'send'},
          {c:'#3A76F0',n:'Signal',i:'security'},
        ].map(a=>`<div style="display:flex;flex-direction:column;align-items:center;gap:5px;">
          <div style="width:40px;height:40px;border-radius:12px;background:${a.c};display:flex;align-items:center;justify-content:center;box-shadow:0 2px 8px rgba(0,0,0,0.2);">
            <span class="ms" style="font-size:1.1rem;color:#fff;font-variation-settings:'FILL' 1;">${a.i}</span>
          </div>
          <span style="font-size:0.58rem;color:#4B5563;font-weight:600;text-align:center;">${a.n}</span>
        </div>`).join('')}
      </div>
    </div>`
  },
  translate: {
    title:'Quick Translator',
    html:`<div style="padding:4px 0;">
      <div style="background:#F3E5F5;border:1px solid #CE93D8;border-radius:12px;padding:12px;margin-bottom:10px;">
        <div style="font-size:0.68rem;font-weight:700;color:#7B1FA2;margin-bottom:6px;">🇬🇧 EN → 🇮🇳 HI</div>
        <p style="font-size:0.82rem;color:#1A1A2E;">"Hello, how are you today?"</p>
      </div>
      <div style="background:#EDE7F6;border:1px solid #B39DDB;border-radius:12px;padding:12px;margin-bottom:10px;">
        <p style="font-size:0.9rem;color:#4527A0;font-weight:600;">नमस्ते, आज आप कैसे हैं?</p>
      </div>
      <div style="display:flex;gap:6px;">
        <div style="flex:1;background:#F3E5F5;border:1px solid #CE93D8;border-radius:8px;padding:8px;text-align:center;font-size:0.7rem;font-weight:700;color:#7B1FA2;display:flex;align-items:center;justify-content:center;gap:4px;">
          <span class="ms" style="font-size:0.9rem;font-variation-settings:'FILL' 1;">volume_up</span> TTS
        </div>
        <div style="flex:1;background:#EDE7F6;border:1px solid #B39DDB;border-radius:8px;padding:8px;text-align:center;font-size:0.7rem;font-weight:700;color:#4527A0;display:flex;align-items:center;justify-content:center;gap:4px;">
          <span class="ms" style="font-size:0.9rem;font-variation-settings:'FILL' 1;">content_copy</span> Copy
        </div>
      </div>
    </div>`
  },
  clipboard: {
    title:'Smart Clipboard',
    html:`<div style="padding:4px 0;">
      <p style="font-size:0.7rem;color:#9CA3AF;margin-bottom:10px;">Recent entries — tap to paste.</p>
      ${[
        {t:'meet.google.com/abc-xyz-def',l:'Link',c:'#DBEAFE',bc:'#BFDBFE',tc:'#1E40AF'},
        {t:'UPI: user@okicici',l:'Payment',c:'#FEF3C7',bc:'#FDE68A',tc:'#92400E'},
        {t:'+91 98765 43210',l:'Phone',c:'#DCFCE7',bc:'#BBF7D0',tc:'#166534'},
        {t:'API_KEY: sk-••••••••',l:'Secret',c:'#FCE7F3',bc:'#FBCFE8',tc:'#9D174D'},
      ].map(e=>`<div style="background:${e.c};border:1px solid ${e.bc};border-radius:9px;padding:9px 12px;margin-bottom:6px;display:flex;align-items:center;gap:8px;">
        <span style="font-size:0.6rem;font-weight:800;color:${e.tc};background:${e.bc};padding:1px 6px;border-radius:4px;flex-shrink:0;">${e.l}</span>
        <span style="font-size:0.72rem;color:#1A1A2E;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${e.t}</span>
      </div>`).join('')}
    </div>`
  },
  notes: {
    title:'Quick Notes',
    html:`<div style="padding:4px 0;">
      ${[
        {pin:true,t:'Buy groceries 🛒',c:'Milk, eggs, avocados, bread'},
        {pin:false,t:'Meeting — Friday 3pm',c:'Discuss Q3 product roadmap...'},
        {pin:false,t:'App ideas 💡',c:'Dark mode toggle for QuickDash...'},
      ].map(n=>`<div style="background:#FAFAFA;border:1px solid #E5E7EB;border-radius:10px;padding:10px 12px;margin-bottom:8px;">
        <div style="display:flex;align-items:center;gap:5px;margin-bottom:3px;">
          ${n.pin?'<span class="ms" style="font-size:0.85rem;color:#F59E0B;font-variation-settings:\'FILL\' 1;">push_pin</span>':''}
          <span style="font-size:0.8rem;font-weight:700;color:#1A1A2E;">${n.t}</span>
        </div>
        <p style="font-size:0.7rem;color:#6B7280;line-height:1.45;">${n.c}</p>
      </div>`).join('')}
    </div>`
  },
  calc: {
    title:'Quick Calculator',
    html:`<div style="padding:4px 0;">
      <div style="background:#F5F5F7;border-radius:12px;padding:14px;margin-bottom:10px;text-align:right;">
        <div style="font-size:0.7rem;color:#9CA3AF;margin-bottom:2px;">2 × (450 + 50)</div>
        <div style="font-size:2.2rem;font-weight:300;color:#1A1A2E;letter-spacing:-0.03em;font-family:'JetBrains Mono',monospace;">1,000</div>
      </div>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:5px;">
        ${['C','±','%','÷','7','8','9','×','4','5','6','−','1','2','3','+','0','.','⌫','='].map((k,i)=>`
        <div style="background:${k==='='?'#4285F4':['÷','×','−','+'].includes(k)?'#EEF2FF':'#FFFFFF'};border:1px solid #E5E7EB;border-radius:8px;padding:8px 4px;text-align:center;font-size:0.85rem;font-weight:600;color:${k==='='?'#fff':['÷','×','−','+'].includes(k)?'#4285F4':['C','±','%'].includes(k)?'#EA4335':'#1A1A2E'};${k==='0'?'grid-column:span 2;':''}">${k}</div>`).join('')}
      </div>
    </div>`
  },
  wifi: {
    title:'Quick Wi-Fi QR Share',
    html:`<div style="text-align:center;padding:4px 0;">
      <div style="background:linear-gradient(135deg,#00897B,#004D40);border-radius:16px;padding:20px;margin-bottom:12px;">
        <div style="width:72px;height:72px;background:#fff;border-radius:12px;margin:0 auto;display:flex;align-items:center;justify-content:center;">
          <span class="ms" style="font-size:2.3rem;color:#009688;font-variation-settings:'FILL' 1;">wifi</span>
        </div>
        <p style="color:#fff;font-size:0.82rem;font-weight:700;margin-top:9px;">HomeNetwork_5GHz</p>
        <p style="color:rgba(255,255,255,0.6);font-size:0.68rem;margin-top:2px;">WPA2 · Scan to connect instantly</p>
      </div>
      <p style="font-size:0.72rem;color:#6B7280;line-height:1.55;">Share Wi-Fi credentials without revealing your password. Guests scan the QR and connect.</p>
    </div>`
  },
  search: {
    title:'Quick Search',
    html:`<div style="padding:4px 0;">
      <div style="background:#F3F4F6;border:1px solid #E5E7EB;border-radius:10px;padding:10px 12px;display:flex;align-items:center;gap:8px;margin-bottom:12px;">
        <span class="ms" style="color:#9CA3AF;font-size:1rem;">search</span>
        <span style="font-size:0.78rem;color:#9CA3AF;">Search anything...</span>
      </div>
      ${[
        {c:'#4285F4',n:'Google',i:'search'},
        {c:'#DE5833',n:'DuckDuckGo',i:'privacy_tip'},
        {c:'#FF0000',n:'YouTube',i:'play_circle'},
        {c:'#181717',n:'GitHub',i:'code'},
        {c:'#636466',n:'Wikipedia',i:'menu_book'},
      ].map(e=>`<div style="background:#F8F9FA;border:1px solid #E5E7EB;border-radius:9px;padding:9px 12px;margin-bottom:6px;display:flex;align-items:center;gap:10px;">
        <span class="ms" style="color:${e.c};font-size:1rem;font-variation-settings:'FILL' 1;">${e.i}</span>
        <span style="font-size:0.78rem;font-weight:600;color:#1A1A2E;">${e.n}</span>
        <span class="ms" style="margin-left:auto;color:#D1D5DB;font-size:0.9rem;">open_in_new</span>
      </div>`).join('')}
    </div>`
  },
  converter: {
    title:'Quick Converter',
    html:`<div style="padding:4px 0;">
      <div style="background:linear-gradient(135deg,rgba(103,58,183,0.1),rgba(161,66,244,0.08));border:1px solid rgba(103,58,183,0.2);border-radius:12px;padding:16px;margin-bottom:12px;">
        <div style="font-size:0.7rem;font-weight:700;color:#7E57C2;margin-bottom:8px;">INR → USD · Live Rate</div>
        <div style="font-size:2rem;font-weight:300;color:#1A1A2E;letter-spacing:-0.03em;">₹ 10,000</div>
        <div style="font-size:1.1rem;color:#673AB7;font-weight:600;margin-top:4px;">= $ 119.64</div>
        <div style="font-size:0.65rem;color:#9CA3AF;margin-top:6px;">1 USD = ₹ 83.58 · Updated just now</div>
      </div>
      <div style="display:flex;gap:6px;flex-wrap:wrap;">
        ${['USD','EUR','GBP','JPY','AED','SGD'].map(c=>`<div style="background:#F5F3FF;border:1px solid #DDD6FE;padding:4px 10px;border-radius:7px;font-size:0.7rem;font-weight:700;color:#6D28D9;">${c}</div>`).join('')}
      </div>
    </div>`
  },
  capture: {
    title:'Quick Capture',
    html:`<div style="padding:4px 0;">
      <div style="background:#FFF3E0;border:1px solid #FFCC80;border-radius:12px;padding:13px;margin-bottom:10px;">
        <div style="display:flex;align-items:center;gap:10px;">
          <div style="width:34px;height:34px;background:#F44336;border-radius:50%;display:flex;align-items:center;justify-content:center;animation:pulse 1.2s ease-in-out infinite;">
            <div style="width:12px;height:12px;background:#fff;border-radius:2px;"></div>
          </div>
          <div>
            <div style="font-size:0.8rem;font-weight:700;color:#E65100;">🔴 Screen Recording Active</div>
            <div style="font-size:0.68rem;color:#F57C00;font-family:'JetBrains Mono',monospace;">00:02:34 · 1080p · 60fps</div>
          </div>
        </div>
      </div>
      <div style="background:#FFFDE7;border:1px solid #FFF176;border-radius:12px;padding:12px;text-align:center;">
        <span class="ms" style="font-size:2rem;color:#F9A825;font-variation-settings:'FILL' 1;">draw</span>
        <p style="font-size:0.72rem;color:#F57F17;margin-top:6px;font-weight:600;">Canvas Annotator · White Board · PDF Export</p>
      </div>
    </div>`
  },
  timer: {
    title:'Quick Timer',
    html:`<div style="text-align:center;padding:4px 0;">
      <div style="background:linear-gradient(135deg,#004D40,#00695C);border-radius:16px;padding:22px;margin-bottom:12px;">
        <div style="font-size:2.8rem;font-weight:200;color:#fff;letter-spacing:0.05em;font-family:'JetBrains Mono',monospace;line-height:1;">00:02:47</div>
        <div style="font-size:0.7rem;color:rgba(255,255,255,0.55);margin-top:6px;">Stopwatch · Lap 3</div>
        <div style="display:flex;gap:8px;justify-content:center;margin-top:14px;">
          <div style="background:rgba(255,255,255,0.12);border-radius:8px;padding:7px 16px;font-size:0.72rem;color:#fff;font-weight:600;">⏱ Lap</div>
          <div style="background:rgba(255,255,255,0.12);border-radius:8px;padding:7px 16px;font-size:0.72rem;color:#fff;font-weight:600;">⏹ Stop</div>
          <div style="background:rgba(255,255,255,0.12);border-radius:8px;padding:7px 16px;font-size:0.72rem;color:#fff;font-weight:600;">↺ Reset</div>
        </div>
      </div>
      <div style="font-size:0.68rem;color:#9CA3AF;line-height:1.7;">
        Lap 3: 01:12 &nbsp;·&nbsp; Lap 2: 00:48 &nbsp;·&nbsp; Lap 1: 00:47
      </div>
    </div>`
  },
};

document.querySelectorAll('.tool-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    const info = TOOLS[btn.dataset.tool];
    if (!info) return;
    grid.style.display = 'none';
    if (detTitle) detTitle.textContent = info.title;
    if (detBody)  detBody.innerHTML   = info.html;
    detail.style.display = 'flex';
  });
});

/* ─── Theme switcher ─── */
const html = document.documentElement;
document.querySelectorAll('.theme-opt').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.theme-opt').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    html.setAttribute('data-theme', btn.dataset.theme || '');
  });
});

/* ─── Animated stat counters ─── */
function animateCounter(el, end, suffix='') {
  const dur = 1800;
  const start = performance.now();
  const from = 0;
  const step = ts => {
    const prog = Math.min((ts - start) / dur, 1);
    const ease = 1 - Math.pow(1 - prog, 3);
    el.textContent = Math.round(from + (end - from) * ease) + suffix;
    if (prog < 1) requestAnimationFrame(step);
  };
  requestAnimationFrame(step);
}

const cRo = new IntersectionObserver(entries => {
  entries.forEach(e => {
    if (e.isIntersecting) {
      const el = e.target;
      const val = el.dataset.count;
      if (val) animateCounter(el, parseInt(val));
      cRo.unobserve(el);
    }
  });
}, { threshold: 0.5 });
document.querySelectorAll('[data-count]').forEach(el => cRo.observe(el));

/* ─── ADB Command Copy ─── */
const btnCopyAdb = document.getElementById('btn-copy-adb');
if (btnCopyAdb) {
  btnCopyAdb.addEventListener('click', () => {
    navigator.clipboard.writeText('adb install -r app-universal-release.apk').then(() => {
      showToast('📋 ADB Command copied!');
    });
  });
}

/* ─── Phone Screen Theme Toggle ─── */
const btnToggleScreen = document.getElementById('btn-toggle-screen');
const phoneScreen = document.getElementById('phone-screen');
if (btnToggleScreen && phoneScreen) {
  btnToggleScreen.addEventListener('click', () => {
    const isDark = phoneScreen.getAttribute('data-screen') === 'dark';
    if (isDark) {
      phoneScreen.removeAttribute('data-screen');
      showToast('☀️ Phone wallpaper: Light');
    } else {
      phoneScreen.setAttribute('data-screen', 'dark');
      showToast('🌙 Phone wallpaper: Dark');
    }
  });
}

/* ─── Keyboard Shortcut (D key to open simulator) ─── */
window.addEventListener('keydown', e => {
  if (e.key === 'd' || e.key === 'D') {
    if (document.activeElement.tagName === 'INPUT' || document.activeElement.tagName === 'TEXTAREA') return;
    if (overlay && overlay.classList.contains('open')) {
      close();
    } else {
      open();
      showToast('⚡ Interactive Dashboard Opened');
    }
  }
});

/* ─── Modals: Privacy Policy & Changelog ─── */
const modalPrivacy   = document.getElementById('modal-privacy');
const modalChangelog = document.getElementById('modal-changelog');

const openModal  = m => m && m.classList.add('open');
const closeModal = m => m && m.classList.remove('open');

// Triggers
['link-privacy', 'link-privacy-footer'].forEach(id => {
  const el = document.getElementById(id);
  if (el) el.addEventListener('click', e => { e.preventDefault(); openModal(modalPrivacy); });
});

['link-changelog', 'link-changelog-footer'].forEach(id => {
  const el = document.getElementById(id);
  if (el) el.addEventListener('click', e => { e.preventDefault(); openModal(modalChangelog); });
});

// Close buttons
const closePriv = document.getElementById('close-privacy');
const closeChange = document.getElementById('close-changelog');

if (closePriv)  closePriv.addEventListener('click', () => closeModal(modalPrivacy));
if (closeChange)closeChange.addEventListener('click', () => closeModal(modalChangelog));

// Mobile changelog trigger
const linkChangelogMob = document.getElementById('link-changelog-mob');
if (linkChangelogMob) {
  linkChangelogMob.addEventListener('click', e => {
    e.preventDefault();
    openModal(modalChangelog);
    if (mobDrawer) mobDrawer.classList.remove('open');
  });
}

// Backdrop click close
[modalPrivacy, modalChangelog].forEach(m => {
  if (m) {
    m.addEventListener('click', e => {
      if (e.target === m) closeModal(m);
    });
  }
});

/* ─── Mobile Drawer Toggle ─── */
const btnMobMenu  = document.getElementById('btn-mobile-menu');
const mobDrawer   = document.getElementById('mobile-drawer');
const iconMobMenu = document.getElementById('icon-mobile-menu');

if (btnMobMenu && mobDrawer) {
  btnMobMenu.addEventListener('click', () => {
    const isOpen = mobDrawer.classList.toggle('open');
    if (iconMobMenu) iconMobMenu.textContent = isOpen ? 'close' : 'menu';
  });

  mobDrawer.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', () => {
      mobDrawer.classList.remove('open');
      if (iconMobMenu) iconMobMenu.textContent = 'menu';
    });
  });
}

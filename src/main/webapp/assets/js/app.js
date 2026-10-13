/**
 * CareFirst Healthcare System - Lightweight UI Interactions
 * Charts, Micro-Animations, Animated Counters, Toast, Command Palette, Slot Picker
 */

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initCounters();
    initCommandPalette();
    initCharts();
    initNotifications();
    initModals();
    initSlotPicker();
});

/* --- 1. Theme Management (Light / Dark) --- */
function initTheme() {
    const savedTheme = localStorage.getItem('carefirst_theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);

    const toggleBtn = document.getElementById('themeToggleBtn');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            const current = document.documentElement.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('carefirst_theme', next);
        });
    }
}

/* --- 2. Animated Number Counters --- */
function initCounters() {
    const counters = document.querySelectorAll('[data-counter]');
    counters.forEach(counter => {
        const target = parseFloat(counter.getAttribute('data-counter')) || 0;
        const duration = 1200; // ms
        const startTime = performance.now();
        const isDecimal = target % 1 !== 0;

        function updateCount(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            // Ease out quad
            const ease = 1 - (1 - progress) * (1 - progress);
            const currentVal = ease * target;

            counter.textContent = isDecimal ? currentVal.toFixed(1) : Math.floor(currentVal).toLocaleString();

            if (progress < 1) {
                requestAnimationFrame(updateCount);
            } else {
                counter.textContent = isDecimal ? target.toFixed(1) : target.toLocaleString();
            }
        }
        requestAnimationFrame(updateCount);
    });
}

/* --- 3. Toast Notifications --- */
function showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span class="toast-icon">${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span>
        <span class="toast-msg">${message}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

/* --- 4. Command Palette (Ctrl+K or Cmd+K) --- */
function initCommandPalette() {
    const backdrop = document.getElementById('commandPalette');
    const input = document.getElementById('paletteInput');
    const openBtn = document.getElementById('openPaletteBtn');

    if (!backdrop) return;

    function openPalette() {
        backdrop.classList.add('open');
        if (input) {
            input.value = '';
            input.focus();
            filterPaletteItems('');
        }
    }

    function closePalette() {
        backdrop.classList.remove('open');
    }

    if (openBtn) {
        openBtn.addEventListener('click', openPalette);
    }

    window.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
            e.preventDefault();
            if (backdrop.classList.contains('open')) closePalette();
            else openPalette();
        } else if (e.key === 'Escape' && backdrop.classList.contains('open')) {
            closePalette();
        }
    });

    backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) closePalette();
    });

    if (input) {
        input.addEventListener('input', (e) => {
            filterPaletteItems(e.target.value.toLowerCase());
        });
    }
}

function filterPaletteItems(term) {
    const items = document.querySelectorAll('.palette-item');
    items.forEach(item => {
        const text = item.textContent.toLowerCase();
        item.style.display = text.includes(term) ? 'flex' : 'none';
    });
}

/* --- 5. Chart.js Initializer --- */
function initCharts() {
    const scriptEl = document.getElementById('chartDataScript');
    if (!scriptEl || typeof Chart === 'undefined') return;

    try {
        const rawJson = scriptEl.textContent;
        const data = JSON.parse(rawJson);

        // Common Chart styling palette
        const colors = {
            peach: '#E8906B',
            peachSoft: 'rgba(232, 144, 107, 0.25)',
            sage: '#7FA88B',
            dustyRose: '#D9A0A0',
            sand: '#D9A55C',
            slate: '#8FA9C4'
        };

        // 1. Appointments per Day (Area Chart)
        const perDayCanvas = document.getElementById('appointmentsPerDayChart');
        if (perDayCanvas && data.appointmentsPerDay) {
            const labels = Object.keys(data.appointmentsPerDay);
            const values = Object.values(data.appointmentsPerDay);

            new Chart(perDayCanvas, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Appointments',
                        data: values,
                        borderColor: colors.peach,
                        backgroundColor: colors.peachSoft,
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: colors.peach,
                        pointRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, grid: { color: 'rgba(243, 227, 216, 0.6)' } },
                        x: { grid: { display: false } }
                    }
                }
            });
        }

        // 2. Status Breakdown (Doughnut Chart)
        const statusCanvas = document.getElementById('statusDoughnutChart');
        if (statusCanvas && data.appointmentsByStatus) {
            const labels = Object.keys(data.appointmentsByStatus);
            const values = Object.values(data.appointmentsByStatus);

            new Chart(statusCanvas, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: values,
                        backgroundColor: [colors.sand, colors.sage, colors.slate, colors.dustyRose, colors.peach]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { boxWidth: 12, padding: 14 } }
                    },
                    cutout: '72%'
                }
            });
        }

        // 3. Top Doctors (Bar Chart)
        const doctorsCanvas = document.getElementById('topDoctorsChart');
        if (doctorsCanvas && data.topDoctors) {
            const labels = Object.keys(data.topDoctors);
            const values = Object.values(data.topDoctors);

            new Chart(doctorsCanvas, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Completed Consultations',
                        data: values,
                        backgroundColor: colors.sage,
                        borderRadius: 8
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, grid: { color: 'rgba(243, 227, 216, 0.6)' } },
                        x: { grid: { display: false } }
                    }
                }
            });
        }

    } catch (e) {
        console.error('Error rendering Chart.js charts', e);
    }
}

/* --- 6. Topbar Notifications Bell --- */
function initNotifications() {
    const bellBtn = document.getElementById('notifBellBtn');
    const dropdown = document.getElementById('notifDropdown');
    const markAllBtn = document.getElementById('markAllReadBtn');

    if (!bellBtn || !dropdown) return;

    bellBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdown.classList.toggle('open');
    });

    document.addEventListener('click', (e) => {
        if (!dropdown.contains(e.target) && e.target !== bellBtn) {
            dropdown.classList.remove('open');
        }
    });

    if (markAllBtn) {
        markAllBtn.addEventListener('click', async () => {
            const csrf = getCsrfToken();
            try {
                const res = await fetch(getContextPath() + '/notifications/read-all', {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': csrf }
                });
                const data = await res.json();
                if (data.success) {
                    showToast('All notifications marked as read', 'success');
                    const badge = document.querySelector('.badge-unread');
                    if (badge) badge.remove();
                }
            } catch (err) {
                console.error('Failed to mark all notifications read', err);
            }
        });
    }
}

/* --- 7. Modals & Drawers --- */
function initModals() {
    document.querySelectorAll('[data-modal-target]').forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-modal-target');
            const modal = document.getElementById(targetId);
            if (modal) modal.classList.add('open');
        });
    });

    document.querySelectorAll('.modal-close, [data-modal-close]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal-backdrop');
            if (modal) modal.classList.remove('open');
        });
    });

    document.querySelectorAll('.modal-backdrop').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('open');
        });
    });
}

/* --- 8. Dynamic Slot Picker --- */
function initSlotPicker() {
    const slotContainer = document.getElementById('slotPickerContainer');
    const doctorSelect = document.getElementById('bookingDoctorSelect');
    const dateInput = document.getElementById('bookingDateInput');
    const hiddenSlotInput = document.getElementById('selectedSlotId');

    if (!slotContainer || !doctorSelect || !dateInput) return;

    async function fetchSlots() {
        const doctorId = doctorSelect.value;
        const date = dateInput.value;
        if (!doctorId || !date) return;

        slotContainer.innerHTML = '<div style="padding:14px;color:var(--text-muted);">Loading available slots...</div>';

        try {
            const res = await fetch(`${getContextPath()}/api/slots?doctorId=${doctorId}&date=${date}`);
            const json = await res.json();

            if (!json.success || !json.data || json.data.length === 0) {
                slotContainer.innerHTML = '<div style="padding:14px;color:var(--text-muted);font-style:italic;">No slots available for this doctor on selected date.</div>';
                return;
            }

            slotContainer.innerHTML = '';
            const grid = document.createElement('div');
            grid.className = 'slot-grid';

            json.data.forEach(slot => {
                const pill = document.createElement('div');
                pill.className = `slot-pill ${slot.isBooked ? 'booked' : ''}`;
                pill.textContent = slot.startTime.substring(0, 5) + ' - ' + slot.endTime.substring(0, 5);

                if (!slot.isBooked) {
                    pill.addEventListener('click', () => {
                        document.querySelectorAll('.slot-pill').forEach(p => p.classList.remove('selected'));
                        pill.classList.add('selected');
                        if (hiddenSlotInput) hiddenSlotInput.value = slot.id;
                    });
                }
                grid.appendChild(pill);
            });

            slotContainer.appendChild(grid);
        } catch (e) {
            slotContainer.innerHTML = '<div style="color:var(--danger);">Error loading slots.</div>';
        }
    }

    doctorSelect.addEventListener('change', fetchSlots);
    dateInput.addEventListener('change', fetchSlots);

    // Initial load if pre-selected
    if (doctorSelect.value && dateInput.value) {
        fetchSlots();
    }
}

/* --- Helpers --- */
function getCsrfToken() {
    const meta = document.querySelector('meta[name="csrf-token"]');
    return meta ? meta.getAttribute('content') : '';
}

function getContextPath() {
    const meta = document.querySelector('meta[name="context-path"]');
    return meta ? meta.getAttribute('content') : '';
}

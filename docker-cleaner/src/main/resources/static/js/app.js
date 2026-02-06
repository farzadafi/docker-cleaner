// script.js
// ====================================
// Application State
// ====================================
let selectedFile = null;
let analysisResult = null;
let fixedContent = null;
let stats = { scans: 0, issues: 0, fixed: 0 };

// Default configuration
const defaultConfig = {
    system_title: 'Dockerfile Security Analyzer',
    system_subtitle: 'تحلیل و رفع خودکار آسیب‌پذیری‌های امنیتی در فایل‌های Docker',
    primary_color: '#3b82f6',
    secondary_color: '#10b981',
    background_color: '#0f172a',
    text_color: '#ffffff',
    accent_color: '#ef4444'
};

// ====================================
// Element SDK Integration
// ====================================
if (window.elementSdk) {
    window.elementSdk.init({
        defaultConfig,
        onConfigChange: async (config) => {
            // Update system title
            const titleEl = document.getElementById('system-title');
            if (titleEl) {
                titleEl.textContent = config.system_title || defaultConfig.system_title;
            }

            // Update system subtitle
            const subtitleEl = document.getElementById('system-subtitle');
            if (subtitleEl) {
                subtitleEl.textContent = config.system_subtitle || defaultConfig.system_subtitle;
            }
        },
        mapToCapabilities: (config) => ({
            recolorables: [
                {
                    get: () => config.primary_color || defaultConfig.primary_color,
                    set: (value) => {
                        config.primary_color = value;
                        window.elementSdk.setConfig({ primary_color: value });
                    }
                },
                {
                    get: () => config.secondary_color || defaultConfig.secondary_color,
                    set: (value) => {
                        config.secondary_color = value;
                        window.elementSdk.setConfig({ secondary_color: value });
                    }
                },
                {
                    get: () => config.background_color || defaultConfig.background_color,
                    set: (value) => {
                        config.background_color = value;
                        window.elementSdk.setConfig({ background_color: value });
                    }
                },
                {
                    get: () => config.text_color || defaultConfig.text_color,
                    set: (value) => {
                        config.text_color = value;
                        window.elementSdk.setConfig({ text_color: value });
                    }
                },
                {
                    get: () => config.accent_color || defaultConfig.accent_color,
                    set: (value) => {
                        config.accent_color = value;
                        window.elementSdk.setConfig({ accent_color: value });
                    }
                }
            ],
            borderables: [],
            fontEditable: undefined,
            fontSizeable: undefined
        }),
        mapToEditPanelValues: (config) => new Map([
            ['system_title', config.system_title || defaultConfig.system_title],
            ['system_subtitle', config.system_subtitle || defaultConfig.system_subtitle]
        ])
    });
}

// ====================================
// DOM Elements
// ====================================
const uploadZone = document.getElementById('upload-zone');
const fileInput = document.getElementById('file-input');
const uploadContent = document.getElementById('upload-content');
const fileSelected = document.getElementById('file-selected');
const fileName = document.getElementById('file-name');
const fileSize = document.getElementById('file-size');
const removeFileBtn = document.getElementById('remove-file');
const analyzeBtn = document.getElementById('analyze-btn');
const analyzeIcon = document.getElementById('analyze-icon');
const analyzeSpinner = document.getElementById('analyze-spinner');
const analyzeText = document.getElementById('analyze-text');
const fixBtn = document.getElementById('fix-btn');
const fixSpinner = document.getElementById('fix-spinner');
const fixText = document.getElementById('fix-text');
const resultsSection = document.getElementById('results-section');
const successSection = document.getElementById('success-section');
const emptyState = document.getElementById('empty-state');
const issuesList = document.getElementById('issues-list');
const issuesCount = document.getElementById('issues-count');
const analyzedFileName = document.getElementById('analyzed-file-name');
const downloadBtn = document.getElementById('download-btn');
const errorToast = document.getElementById('error-toast');
const errorMessage = document.getElementById('error-message');
const closeError = document.getElementById('close-error');
const apiToggle = document.getElementById('api-toggle');
const apiContent = document.getElementById('api-content');
const apiChevron = document.getElementById('api-chevron');

// ====================================
// Utility Functions
// ====================================
function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function showError(message) {
    errorMessage.textContent = message;
    errorToast.classList.remove('hidden');
    setTimeout(() => {
        errorToast.classList.add('hidden');
    }, 5000);
}

function updateStats() {
    document.getElementById('total-scans').textContent = stats.scans;
    document.getElementById('total-issues').textContent = stats.issues;
    document.getElementById('total-fixed').textContent = stats.fixed;
}

// ====================================
// File Upload Handlers
// ====================================
uploadZone.addEventListener('click', () => fileInput.click());

uploadZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadZone.classList.add('dragover');
});

uploadZone.addEventListener('dragleave', () => {
    uploadZone.classList.remove('dragover');
});

uploadZone.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadZone.classList.remove('dragover');
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleFileSelect(files[0]);
    }
});

fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
        handleFileSelect(e.target.files[0]);
    }
});

function handleFileSelect(file) {
    selectedFile = file;
    fileName.textContent = file.name;
    fileSize.textContent = formatFileSize(file.size);
    uploadContent.classList.add('hidden');
    fileSelected.classList.remove('hidden');
    analyzeBtn.disabled = false;

    // Reset previous results
    resultsSection.classList.add('hidden');
    successSection.classList.add('hidden');
    emptyState.classList.remove('hidden');
    fixBtn.disabled = true;
    analysisResult = null;
    fixedContent = null;
}

removeFileBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    selectedFile = null;
    fileInput.value = '';
    uploadContent.classList.remove('hidden');
    fileSelected.classList.add('hidden');
    analyzeBtn.disabled = true;
    fixBtn.disabled = true;
    resultsSection.classList.add('hidden');
    successSection.classList.add('hidden');
    emptyState.classList.remove('hidden');
});

// ====================================
// API Toggle Handler
// ====================================
apiToggle.addEventListener('click', () => {
    apiContent.classList.toggle('hidden');
    apiChevron.classList.toggle('rotate-180');
});

// ====================================
// Analysis Handler
// ====================================
analyzeBtn.addEventListener('click', async () => {
    if (!selectedFile) return;

    // Show loading state
    analyzeBtn.disabled = true;
    analyzeIcon.classList.add('hidden');
    analyzeSpinner.classList.remove('hidden');
    analyzeText.textContent = 'در حال تحلیل...';

    try {
        const formData = new FormData();
        formData.append('file', selectedFile);

        const apiUrl = document.getElementById('api-analyze-url').value;

        // Simulated API call for demo (replace with actual fetch)
        // In production, uncomment the fetch call below
        /*
        const response = await fetch(apiUrl, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('خطا در برقراری ارتباط با سرور');
        }

        analysisResult = await response.json();
        */

        // Demo data for presentation
        await new Promise(resolve => setTimeout(resolve, 2000));
        analysisResult = {
            filename: selectedFile.name,
            issues: [
                {
                    id: 1,
                    severity: 'high',
                    title: 'استفاده از latest tag',
                    description: 'استفاده از تگ latest باعث عدم قطعیت در نسخه ایمیج می‌شود',
                    line: 1,
                    recommendation: 'از تگ‌های نسخه‌دار مشخص استفاده کنید'
                },
                {
                    id: 2,
                    severity: 'critical',
                    title: 'اجرا با کاربر root',
                    description: 'کانتینر با دسترسی root اجرا می‌شود که خطر امنیتی دارد',
                    line: 5,
                    recommendation: 'یک کاربر غیر root ایجاد و از آن استفاده کنید'
                },
                {
                    id: 3,
                    severity: 'medium',
                    title: 'عدم استفاده از multi-stage build',
                    description: 'فایل‌های غیرضروری در ایمیج نهایی باقی می‌مانند',
                    line: null,
                    recommendation: 'از multi-stage build برای کاهش حجم استفاده کنید'
                },
                {
                    id: 4,
                    severity: 'low',
                    title: 'عدم تعیین HEALTHCHECK',
                    description: 'وضعیت سلامت کانتینر قابل بررسی نیست',
                    line: null,
                    recommendation: 'دستور HEALTHCHECK را اضافه کنید'
                },
                {
                    id: 5,
                    severity: 'high',
                    title: 'استفاده از ADD به جای COPY',
                    description: 'دستور ADD قابلیت‌های اضافی دارد که ممکن است ناخواسته باشند',
                    line: 8,
                    recommendation: 'برای کپی فایل‌های ساده از COPY استفاده کنید'
                }
            ]
        };

        // Update stats
        stats.scans++;
        stats.issues += analysisResult.issues.length;
        updateStats();

        // Display results
        displayResults(analysisResult);

    } catch (error) {
        showError(error.message || 'خطا در تحلیل فایل');
    } finally {
        // Reset button state
        analyzeBtn.disabled = false;
        analyzeIcon.classList.remove('hidden');
        analyzeSpinner.classList.add('hidden');
        analyzeText.textContent = 'شروع بررسی امنیتی';
    }
});

function displayResults(result) {
    emptyState.classList.add('hidden');
    successSection.classList.add('hidden');
    resultsSection.classList.remove('hidden');

    analyzedFileName.textContent = `فایل: ${result.filename}`;
    issuesCount.textContent = `${result.issues.length} مشکل`;

    issuesList.innerHTML = '';

    result.issues.forEach((issue, index) => {
        const severityColors = {
            critical: 'bg-red-600',
            high: 'bg-orange-500',
            medium: 'bg-yellow-500',
            low: 'bg-blue-500'
        };

        const severityLabels = {
            critical: 'بحرانی',
            high: 'بالا',
            medium: 'متوسط',
            low: 'پایین'
        };

        const issueCard = document.createElement('div');
        issueCard.className = 'issue-card rounded-lg p-4 fade-in';
        issueCard.style.animationDelay = `${index * 0.1}s`;

        issueCard.innerHTML = `
            <div class="flex items-start gap-3">
                <div class="flex-shrink-0 w-8 h-8 rounded-lg bg-red-500/20 flex items-center justify-center mt-1">
                    <svg class="w-4 h-4 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    </svg>
                </div>
                <div class="flex-grow">
                    <div class="flex items-center gap-2 mb-1">
                        <span class="font-semibold text-white">${issue.title}</span>
                        <span class="px-2 py-0.5 rounded text-xs ${severityColors[issue.severity]} text-white">
                            ${severityLabels[issue.severity]}
                        </span>
                        ${issue.line ? `<span class="text-xs text-slate-500">خط ${issue.line}</span>` : ''}
                    </div>
                    <p class="text-sm text-slate-400 mb-2">${issue.description}</p>
                    <div class="flex items-center gap-2 text-xs text-green-400">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                        </svg>
                        <span>${issue.recommendation}</span>
                    </div>
                </div>
            </div>
        `;

        issuesList.appendChild(issueCard);
    });

    // Enable fix button if there are issues
    fixBtn.disabled = result.issues.length === 0;
}

// ====================================
// Fix Handler
// ====================================
fixBtn.addEventListener('click', async () => {
    if (!analysisResult) return;

    // Show loading state
    fixBtn.disabled = true;
    fixSpinner.classList.remove('hidden');
    fixText.textContent = 'در حال رفع مشکلات...';

    try {
        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('issues', JSON.stringify(analysisResult.issues));

        const apiUrl = document.getElementById('api-fix-url').value;

        // Simulated API call for demo
        /*
        const response = await fetch(apiUrl, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('خطا در رفع مشکلات');
        }

        const result = await response.json();
        fixedContent = result.fixedContent;
        */

        // Demo data
        await new Promise(resolve => setTimeout(resolve, 2500));
        fixedContent = `# Fixed Dockerfile - Security Issues Resolved
FROM node:18.17.0-alpine

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Use COPY instead of ADD for simple file copying
COPY package*.json ./
RUN npm ci --only=production

COPY --chown=appuser:appgroup . .

# Switch to non-root user
USER appuser

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

EXPOSE 3000

CMD ["node", "server.js"]`;

        // Update stats
        stats.fixed += analysisResult.issues.length;
        updateStats();

        // Show success section
        resultsSection.classList.add('hidden');
        successSection.classList.remove('hidden');

    } catch (error) {
        showError(error.message || 'خطا در رفع مشکلات');
    } finally {
        fixBtn.disabled = false;
        fixSpinner.classList.add('hidden');
        fixText.textContent = 'رفع خودکار مشکلات';
    }
});

// ====================================
// Download Handler
// ====================================
downloadBtn.addEventListener('click', () => {
    if (!fixedContent) return;

    const blob = new Blob([fixedContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'Dockerfile.fixed';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
});

// ====================================
// Error Toast Handler
// ====================================
closeError.addEventListener('click', () => {
    errorToast.classList.add('hidden');
});
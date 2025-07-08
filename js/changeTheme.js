(function () {
    const storedTheme = localStorage.getItem('theme');
    document.documentElement.setAttribute('data-theme',
        storedTheme || 'dark'
    );
})();

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
}
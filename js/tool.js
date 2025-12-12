(function () {
    const storedTheme = localStorage.getItem('theme');
    document.documentElement.setAttribute('data-theme',
        storedTheme || 'light'
    );
})();

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
}

document.addEventListener("DOMContentLoaded", function () {
    const viewer = document.createElement('div');
    viewer.id = 'imgViewer';
    viewer.innerHTML = '<img src="">';
    document.body.appendChild(viewer);

    const viewerImg = viewer.querySelector('img');

    document.querySelectorAll('img').forEach(img => {
        img.addEventListener('click', () => {
            viewerImg.src = img.src;
            viewer.style.display = 'flex';
        });
    });

    viewer.addEventListener('click', () => {
        viewer.style.display = 'none';
    });
});





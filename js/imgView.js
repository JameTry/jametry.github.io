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

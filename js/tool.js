document.addEventListener("DOMContentLoaded", function () {
    //图片预览====
    const viewer = document.createElement('div');
    viewer.id = 'imgViewer';
    viewer.innerHTML = '<img src="">';
    document.body.appendChild(viewer);

    const viewerImg = viewer.querySelector('img');

    document.querySelectorAll('img').forEach(img => {
        img.addEventListener('click', () => {
            if (img.hasAttribute('no')) {
                return; // 跳过预览
            }
            viewerImg.src = img.src;
            viewer.style.display = 'flex';
        });
    });

    viewer.addEventListener('click', () => {
        viewer.style.display = 'none';
    });

// ===== 导航 =====
// 获取标题，只获取 h2
    const headers = document.querySelectorAll('article h2');

    if (headers.length >= 2) {

        const toc = document.createElement('nav');
        toc.className = 'toc';

        const rootUl = document.createElement('ul');

        const links = [];
        headers.forEach((h, i) => {

            const id = 'h-' + i;
            h.id = id;

            const li = document.createElement('li');
            const a = document.createElement('a');

            a.href = 'javascript:void(0)';
            a.textContent = h.textContent;

            a.addEventListener('click', () => {

                h.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });

            });

            li.appendChild(a);
            rootUl.appendChild(li);

            links.push(a);

        });

        toc.appendChild(rootUl);

        document.querySelector('article').prepend(toc);
        function setActive() {
            let index = 0;
            const offset = 120;
            headers.forEach((h, i) => {
                const rect = h.getBoundingClientRect();
                if (rect.top - offset <= 0) {
                    index = i;
                }
            });

            links.forEach(a => {
                a.classList.remove('active');
            });

            if (links[index]) {
                links[index].classList.add('active');
            }
        }

        setActive();

        let ticking = false;

        window.addEventListener('scroll', () => {
            if (!ticking) {
                window.requestAnimationFrame(() => {
                    setActive();
                    ticking = false;
                });
                ticking = true;
            }
        });
    }
});


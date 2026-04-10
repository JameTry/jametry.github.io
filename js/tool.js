document.addEventListener("DOMContentLoaded", function () {
    //图片预览====
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


    //导航========
    // ===== 获取标题 =====
    const headers = document.querySelectorAll('h1, h2, h3,h4');
    if (headers.length < 2) return;

    // ===== 创建 TOC =====
    const toc = document.createElement('nav');
    toc.className = 'toc';

    const rootUl = document.createElement('ul');

    let currentH1Li = null;
    let currentH2Li = null;

    const links = [];

    headers.forEach((h, i) => {
        const id = 'h-' + i;
        h.id = id;

        const li = document.createElement('li');
        const a = document.createElement('a');

        a.href = '#' + id;
        a.textContent = h.textContent;

        li.appendChild(a);
        links.push(a);

        // ===== 构建层级 =====
        if (h.tagName === 'H2') {
            rootUl.appendChild(li);
            currentH1Li = li;
            currentH2Li = null;
        } else if (h.tagName === 'H3') {
            if (!currentH1Li) {
                rootUl.appendChild(li);
            } else {
                let subUl = currentH1Li.querySelector('ul');
                if (!subUl) {
                    subUl = document.createElement('ul');
                    currentH1Li.appendChild(subUl);
                }
                subUl.appendChild(li);
            }
            currentH2Li = li;
        } else if (h.tagName === 'H4') {
            if (!currentH2Li) {
                rootUl.appendChild(li);
            } else {
                let subUl = currentH2Li.querySelector('ul');
                if (!subUl) {
                    subUl = document.createElement('ul');
                    currentH2Li.appendChild(subUl);
                }
                subUl.appendChild(li);
            }
        }
    });

    toc.appendChild(rootUl);
    document.querySelector('article').prepend(toc);

    // ===== 滚动激活 =====
    function setActive() {
        let index = 0;
        const offset = 100; // 顶部偏移（可调）

        headers.forEach((h, i) => {
            const rect = h.getBoundingClientRect();
            if (rect.top - offset <= 0) {
                index = i;
            }
        });

        // 清除所有 active
        links.forEach(a => a.classList.remove('active'));

        const activeLink = links[index];
        if (!activeLink) return;

        // 当前项高亮
        activeLink.classList.add('active');

        // 父级联动高亮
        let parent = activeLink.parentElement;

        while (parent && parent !== document) {
            if (parent.tagName === 'LI') {
                const parentLink = parent.querySelector(':scope > a');
                if (parentLink) {
                    parentLink.classList.add('active');
                }
            }
            parent = parent.parentElement;
        }
    }

    // 初始执行
    setActive();

    // ===== 滚动优化（requestAnimationFrame）=====
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
});


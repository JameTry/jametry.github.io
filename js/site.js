(function () {
    'use strict';

    var THEME_KEY = 'jame-theme';


    function effectiveIsDark() {
        var forced = document.documentElement.getAttribute('data-theme');
        if (forced === 'dark') return true;
        if (forced === 'light') return false;
        return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }

    function setTheme(value) {
        if (value) {
            document.documentElement.setAttribute('data-theme', value);
        } else {
            document.documentElement.removeAttribute('data-theme');
        }
        try {
            if (value) {
                localStorage.setItem(THEME_KEY, value);
            } else {
                localStorage.removeItem(THEME_KEY);
            }
        } catch (err) {
        }
    }

    function initTheme() {
        var btn = document.querySelector('.theme-toggle');
        if (!btn) return;
        btn.addEventListener('click', function () {
            setTheme(effectiveIsDark() ? 'light' : 'dark');
        });
    }


    function initLightbox() {
        var images = document.querySelectorAll('.prose img, .poems img');
        if (!images.length) return;

        var box = document.createElement('div');
        box.id = 'lightbox';
        box.setAttribute('role', 'dialog');
        box.setAttribute('aria-modal', 'true');
        box.innerHTML = '<img alt="">';
        document.body.appendChild(box);

        var picture = box.querySelector('img');

        function open(src, alt) {
            picture.src = src;
            picture.alt = alt || '';
            box.classList.add('is-open');
        }

        function close() {
            box.classList.remove('is-open');
            picture.removeAttribute('src');
        }

        Array.prototype.forEach.call(images, function (img) {
            if (img.hasAttribute('no')) return;
            img.style.cursor = 'zoom-in';
            img.addEventListener('click', function () {
                open(img.currentSrc || img.src, img.alt);
            });
        });

        box.addEventListener('click', close);
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') close();
        });
    }


    function initToc() {
        var article = document.querySelector('.prose, .poems');
        if (!article) return;

        var heads = article.querySelectorAll('h2');
        if (heads.length < 3) return;

        var nav = document.createElement('nav');
        nav.className = 'toc';
        nav.setAttribute('aria-label', '目录');

        var list = document.createElement('ul');
        var links = [];

        Array.prototype.forEach.call(heads, function (head, index) {
            var id = 'sec-' + index;
            head.id = id;

            var item = document.createElement('li');
            var link = document.createElement('a');
            link.href = '#' + id;
            link.textContent = head.textContent.trim();

            item.appendChild(link);
            list.appendChild(item);
            links.push(link);
        });

        nav.appendChild(list);

        var anchor = document.querySelector('.side-nav');
        if (anchor && anchor.parentNode) {
            anchor.parentNode.insertBefore(nav, anchor.nextSibling);
        } else {
            article.parentNode.insertBefore(nav, article);
        }

        var ticking = false;

        function highlight() {
            var current = 0;
            for (var i = 0; i < heads.length; i++) {
                if (heads[i].getBoundingClientRect().top - 120 <= 0) current = i;
            }
            links.forEach(function (link, i) {
                link.classList.toggle('active', i === current);
            });
        }

        highlight();

        window.addEventListener('scroll', function () {
            if (ticking) return;
            ticking = true;
            window.requestAnimationFrame(function () {
                highlight();
                ticking = false;
            });
        }, { passive: true });
    }

    function boot() {
        initTheme();
        initLightbox();
        initToc();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot);
    } else {
        boot();
    }
})();

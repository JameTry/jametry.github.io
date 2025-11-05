window.requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame ||
    window.webkitRequestAnimationFrame || window.msRequestAnimationFrame;
let starDensity = .216;
let speedCoeff = .05;
let width;
let height;
let starCount;
let circleRadius;
let circleCenter;
let first = true;
let giantColor = '180,184,240';
let starColor = '226,228,214';
let cometColor = '108,152,253';
let canva = document.getElementById('universe');
let stars = [];
let universe;
let animationTime = 0;
// 初始化
windowResizeHandler();
window.addEventListener('resize', windowResizeHandler, false);
createUniverse();

function createUniverse() {
    universe = canva.getContext('2d');
    generateStars();
    draw();
}

function generateStars() {
    stars = [];
    for (let i = 0; i < starCount; i++) {
        stars[i] = new Star();
        stars[i].reset();
    }
}

function draw() {
    if (!universe) return;
    universe.clearRect(0, 0, width, height);
    animationTime += 0.01;

    const starsLength = stars.length;
    for (let i = 0; i < starsLength; i++) {
        const star = stars[i];
        star.move();
        star.fadeIn();
        star.fadeOut();
        star.updateTwinkle();
        star.draw();
    }
    window.requestAnimationFrame(draw);
}

function Star() {
    this.twinkleSpeed = 0;
    this.twinkleOffset = 0;
    this.twinkleIntensity = 0;
    this.isTwinkling = false;

    this.reset = function () {
        this.giant = getProbability(3);
        this.comet = this.giant || first ? false : getProbability(10);
        this.x = getRandInterval(0, width);
        this.y = getRandInterval(0, height);
        this.r = getRandInterval(1.1, 2.6);
        this.dx = getRandInterval(speedCoeff, 0.01 + speedCoeff) + (this.comet ? speedCoeff * getRandInterval(50, 120) : 0) + speedCoeff * 2;
        this.dy = getRandInterval(speedCoeff, 0.01 + speedCoeff) - (this.comet ? speedCoeff * getRandInterval(50, 120) : 0);
        this.fadingOut = null;
        this.fadingIn = true;
        this.opacity = 0;
        this.opacityTresh = getRandInterval(.2, this.comet ? 0.6 : 1);
        this.do = getRandInterval(0.0005, 0.002) + (this.comet ? 0.001 : 0);

        if (!this.giant && !this.comet) {
            this.isTwinkling = getProbability(70);
            if (this.isTwinkling) {
                this.twinkleSpeed = getRandInterval(3.5, 6.5);
                this.twinkleOffset = Math.random() * Math.PI * 2;
                this.twinkleIntensity = getRandInterval(0.2, 0.7);
            }
        } else {
            this.isTwinkling = false;
        }
    };

    this.updateTwinkle = function () {
        if (this.isTwinkling && !this.fadingIn && !this.fadingOut) {
            const twinkle = Math.sin(animationTime * this.twinkleSpeed + this.twinkleOffset);
            const baseFactor = (twinkle + 1) / 2;
            this.twinkleFactor = 1 - this.twinkleIntensity * (1 - baseFactor);

        } else {
            this.twinkleFactor = 1;
        }
    };

    this.fadeIn = function () {
        if (this.fadingIn) {
            this.fadingIn = this.opacity <= this.opacityTresh;
            this.opacity += this.do;
        }
    };

    this.fadeOut = function () {
        if (this.fadingOut) {
            this.fadingOut = this.opacity >= 0;
            this.opacity -= this.do / 2;
            if (this.x > width || this.y < 0) {
                this.fadingOut = false;
                this.reset();
            }
        }
    };

    this.draw = function () {
        universe.beginPath();

        if (this.giant) {
            universe.fillStyle = 'rgba(' + giantColor + ',' + this.opacity + ')';
            universe.arc(this.x, this.y, 2, 0, 2 * Math.PI, false);
        } else if (this.comet) {
            universe.fillStyle = 'rgba(' + cometColor + ',' + this.opacity + ')';
            universe.arc(this.x, this.y, 1.5, 0, 2 * Math.PI, false);
            for (let i = 0; i < 30; i++) {
                universe.fillStyle = 'rgba(' + cometColor + ',' + (this.opacity - (this.opacity / 20) * i) + ')';
                universe.rect(this.x - this.dx / 4 * i, this.y - this.dy / 4 * i, 2, 2);
                universe.fill();
            }
        } else {
            let finalOpacity = this.opacity;

            if (this.isTwinkling && !this.fadingIn && !this.fadingOut) {
                finalOpacity = this.opacity * this.twinkleFactor;

                finalOpacity = Math.min(this.opacityTresh, finalOpacity);
            }

            universe.fillStyle = 'rgba(' + starColor + ',' + finalOpacity + ')';
            universe.rect(this.x, this.y, this.r, this.r);
        }

        universe.closePath();
        universe.fill();
    };

    this.move = function () {
        this.x += this.dx;
        this.y += this.dy;
        if (this.fadingOut === false) {
            this.reset();
        }
        if (this.x > width - (width / 4) || this.y < 0) {
            this.fadingOut = true;
        }
    };

    (function () {
        setTimeout(function () {
            first = false;
        }, 50)
    })()
}

function getProbability(percents) {
    return ((Math.floor(Math.random() * 1000) + 1) < percents * 10);
}

function getRandInterval(min, max) {
    return (Math.random() * (max - min) + min);
}

function windowResizeHandler() {

    width = document.documentElement.clientWidth;
    height = document.documentElement.clientHeight;

    starCount = Math.floor(width * starDensity);
    circleRadius = Math.min(width, height) / 2;
    circleCenter = {x: width / 2, y: height / 2};

    canva.width = width;
    canva.height = height;

    generateStars();
}


document.addEventListener('DOMContentLoaded', () => {
    const trigger = document.getElementById('swipe-trigger');
    const mainContent = document.getElementById('main-content');
    const closeBtn = document.getElementById('close-panel-btn');

    let isDragging = false;
    let startY = 0;
    let currentTranslateY = 0;
    const threshold = window.innerHeight / 4;

    function openPanel() {
        mainContent.style.transform = `translateY(-${window.innerHeight}px)`;
        mainContent.classList.add('shifted');
        mainContent.style.transition = 'transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1)';
        mainContent.style.overflowY = 'hidden';
    }

    function closePanel() {
        mainContent.style.transform = `translateY(0)`;
        mainContent.classList.remove('shifted');
        mainContent.style.transition = 'transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1)';
        mainContent.style.overflowY = 'auto';
    }

    function onDragStart(e) {
        if (mainContent.classList.contains('shifted')) return;


        isDragging = true;
        startY = e.clientY || e.touches[0].clientY;
        mainContent.style.transition = 'none';

        const transformMatch = mainContent.style.transform.match(/translateY\(([^)]+)px\)/);
        currentTranslateY = transformMatch ? parseFloat(transformMatch[1]) : 0;

        document.addEventListener('mousemove', onDrag, {passive: false});
        document.addEventListener('mouseup', onDragEnd);
        document.addEventListener('touchmove', onDrag, {passive: false});
        document.addEventListener('touchend', onDragEnd);

        e.preventDefault();
    }

    trigger.addEventListener('mousedown', onDragStart);
    trigger.addEventListener('touchstart', onDragStart, {passive: false});


    function onDrag(e) {
        if (!isDragging) return;

        e.preventDefault();

        const currentY = e.clientY || e.touches[0].clientY;
        const deltaY = startY - currentY; // 向上拖动时 deltaY 为正值

        if (deltaY > 0 && !mainContent.classList.contains('shifted')) {
            let newTranslateY = currentTranslateY - deltaY;
            newTranslateY = Math.max(-window.innerHeight, newTranslateY);
            mainContent.style.transform = `translateY(${newTranslateY}px)`;
        }
    }

    function onDragEnd(e) {
        if (!isDragging) return;
        isDragging = false;

        mainContent.style.transition = 'transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1)';

        document.removeEventListener('mousemove', onDrag);
        document.removeEventListener('mouseup', onDragEnd);
        document.removeEventListener('touchmove', onDrag);
        document.removeEventListener('touchend', onDragEnd);

        const transformStyle = mainContent.style.transform;
        const currentPanelPosition = transformStyle ? parseFloat(transformStyle.replace('translateY(', '').replace('px)', '')) : 0;

        if (currentPanelPosition <= -threshold) {
            openPanel();
        } else {
            closePanel();
        }
    }

    closeBtn.addEventListener('click', () => {
        closePanel();
    });

    let clickPrevented = false;

    trigger.addEventListener('mousemove', () => {
        if (isDragging) clickPrevented = true;
    });

    trigger.addEventListener('touchmove', () => {
        if (isDragging) clickPrevented = true;
    });

    trigger.addEventListener('click', (e) => {
        if (clickPrevented) {
            clickPrevented = false;
            return;
        }

        if (mainContent.classList.contains('shifted')) {
            closePanel();
        } else {
            // openPanel();
        }
    });

    trigger.addEventListener('mousedown', () => {
        clickPrevented = false;
    });
    trigger.addEventListener('touchstart', () => {
        clickPrevented = false;
    }, {passive: true});
});


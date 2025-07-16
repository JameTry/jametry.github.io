var id;
(function () {
    let currentUrl = window.location.href;
    let strings = currentUrl.split("/");
    id= strings[strings.length - 1];

    fetch(`https://like.jame.work/like/${id}`)
        .then(res => res.json())
        .then(data => updateLikeCount(data.likes));
})();

let liked = false;

function like() {
    if (liked) {
        return
    }
    fetch(`https://like.jame.work/like/${id}`, {method: 'POST'})
        .then(res => res.json())
        .then(data => {
            if (data.success){
                liked = true;
                updateLikeCount(data.likes);
            }
        });
}

function updateLikeCount(likes) {
    document.getElementById('likeNumber').innerHTML = likes;
}
(function () {
    let currentUrl = window.location.href;
    let strings = currentUrl.split("/");
    var id = strings[strings.length - 1];

    fetch(`/like/${id}`)
        .then(res => res.json())
        .then(data => console.log(data.likes));
})();

let liked = false;

function like() {
    if (liked) {
        return
    }
    fetch(`/like/${id}`, {method: 'POST'})
        .then(res => res.json())
        .then(data => {
            if (data.success) updateLikeCount(data.likes);
        });
}

function updateLikeCount(likes) {
    liked = true;
    document.getElementById('likeNumber').innerHTML = likes;
}
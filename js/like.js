var id;
let likes=0;
(function () {
    let currentUrl = window.location.href;
    let strings = currentUrl.split("/");
    id= strings[strings.length - 1];

    fetch(`https://like.jame.work/like/${id}`)
        .then(res => res.json())
        .then(data => {
            if(data.likes instanceof Number){
                likes=data.likes;
                updateLikeCount(data.likes)
            }
        });
})();

let liked = false;

function like() {
    if (liked) {
        alert("你已经点过赞了")
        return
    }
    fetch(`https://like.jame.work/like/${id}`, {method: 'POST'})
        .then(res => res.json())
        .then(data => {
            if (data.success){
                liked = true;
                updateLikeCount(likes+1);
            }else{
                alert("你已经点过赞了")
            }
        });
}

function updateLikeCount(likes) {
    document.getElementById('likeNumber').innerHTML = likes;
}
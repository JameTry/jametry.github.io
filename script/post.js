var address = window.location.href
let addressSplit = address.split("/")
let postNum = addressSplit[addressSplit.length - 1]
let havePrev = true;
if (postNum == "1") {
    havePrev = false;
    document.getElementById("prev").style.display = "none"
}


function prev() {
    if (havePrev) {
        addressSplit[addressSplit.length - 1] = parseInt(postNum) - 1;
        window.location.href = `${addressSplit.join("/")}`;
    }
}

function next() {
    addressSplit[addressSplit.length - 1] = parseInt(postNum) + 1;
    window.location.href = `${addressSplit.join("/")}`;
}


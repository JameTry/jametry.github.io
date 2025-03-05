var address = window.location.href
let addressSplit = address.split("/")
let postNum = addressSplit[addressSplit.length - 1]


if (postNum == "1") {
    document.getElementById("prev").style.display = "none"
}

addressSplit[addressSplit.length - 1] = parseInt(postNum) - 1
document.getElementById("prev").href = `${addressSplit.join("/")}`;
addressSplit[addressSplit.length - 1] = parseInt(postNum) + 2;
document.getElementById("next").href = `${addressSplit.join("/")}`;



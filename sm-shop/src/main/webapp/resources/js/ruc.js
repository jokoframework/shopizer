function calculateDV(ruc) {
    if(ruc === "") { return "" }
    rucNumber = String(ruc);
    baseMax = Number(11);
    var total = 0;
    var rest = 0;
    var k = 0;
    var auxNumber = 0;
    var dv = 0;
    var numberAl = "";
    for (var i = 0; i < rucNumber.length; i++) {
        var c = Number(rucNumber.charAt(i));
        numberAl += c.toString();
    }
    k = 2;
    total = 0;
    for (var i = numberAl.length - 1; i >= 0; i --) {
        if(k > baseMax){k = 2};
        auxNumber = Number(numberAl.charAt(i));
        total += (auxNumber * k);
        k = k + 1;
    }
    rest = total % 11;
    if(rest > 1){dv  = 11 - rest} else { dv = 0};
    return dv;
}
function csvToJson(lines) {
    const result = [];
    const headerWithoutTrim = lines[0].split(",");
    const headers = headerWithoutTrim.map(element => element.trim().toLowerCase())
    for (let i = 1; i < lines.length; i++) {
        const obj = {};
        const currentLine = lines[i].split(",");
        for (let j = 0; j < headers.length; j++) {
            if (currentLine[j] == "") {
                obj[headers[j]] = "null"
                continue;
            }
            if (currentLine[j] == undefined) {
                obj[headers[j]] = currentLine[j]
            } else {
                obj[headers[j]] = currentLine[j].trim();
            }
        }
        result.push(obj);
    }
    return result;
}


function arrangeDependencies(payload) {
    for (let field in payload) {
        for (let key in payload[field]) {
            if (key == "dependentOn") {
                assignDependencies(payload, field);
            }
        }
    }
    return payload;
}

function assignDependencies(payload, field) {
    payload[field]["dependencies"] = [{
        "dependentOn": payload[field]["dependentOn"],
        "expectedDependentFieldValue": payload[field]["expectedDependentFieldValue"],
        "expectedCurrentFieldValue": payload[field]["expectedCurrentFieldValue"]
    }];
}

function convertPayloadToJsonArray(payload) {
    console.log(`payload ${payload}`)
    let jsonArray = []
    let index = 0;
    payload = arrangeDependencies(payload);
    for (let field in payload) {
        jsonArray[index++] = payload[field];
    }
    console.log(jsonArray);
    return jsonArray;
}

function lowerCase(data, field = "") {
    if (field == 'pattern') {
        return data
    }
    return String(data).toLowerCase()
}
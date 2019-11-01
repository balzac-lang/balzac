define(["codemirror", "codemirror/addon/mode/simple"], function(CodeMirror, SimpleMode) {
	var keywords = "AIAO|AINO|AISO|BTC|SIAO|SINO|SISO|_|absLock|address|assert|between|block|bool|boolean|checkBlock|checkBlockDelay|checkDate|checkTimeDelay|const|date|else|eval|false|fees|from|fun|hash|hash160|hash256|if|import|input|int|key|mainnet|max|min|network|of|output|package|participant|private|pubkey|relLock|ripemd160|sha1|sha256|sig|signature|size|string|testnet|then|this|toAddress|toPubkey|transaction|true|txid|value|versig|void";
	CodeMirror.defineSimpleMode("xtext/balzac", {
		start: [
			{token: "comment", regex: "\\/\\/.*$"},
			{token: "comment", regex: "\\/\\*", next : "comment"},
			{token: "string", regex: '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'},
			{token: "string", regex: "['](?:(?:\\\\.)|(?:[^'\\\\]))*?[']"},
			{token: "constant.numeric", regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"},
			{token: "lparen", regex: "[\\[({]"},
			{token: "rparen", regex: "[\\])}]"},
			{token: "keyword", regex: "\\b(?:" + keywords + ")\\b"}
		],
		comment: [
			{token: "comment", regex: ".*?\\*\\/", next : "start"},
			{token: "comment", regex: ".+"}
		],
		meta: {
			dontIndentStates: ["comment"],
			lineComment: "//"
		}
	});
});

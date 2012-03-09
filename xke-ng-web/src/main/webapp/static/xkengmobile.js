//========================================
//Local Storage
//========================================
var LocalStorageAPI = {};
	LocalStorageAPI.confId = 'selectedConf';
	LocalStorageAPI.setConf = function(conf) {
		localStorage[this.confId] = JSON.stringify(conf);
	};
	LocalStorageAPI.getConf = function() {
		return JSON.parse(localStorage[this.confId]);
	};
	LocalStorageAPI.getSession = function(sessId) {
		var slots = this.getConf().slots;
		for(slotIndex in slots) {
			var sessions = slots[slotIndex].sessions;
			for(sessionIndex in sessions) {
				var session = sessions[sessionIndex];
				if(session.id == sessId) {
					return session;
				}
			}
		}
		return null;
	};
	LocalStorageAPI.setSession = function(session) {
		var conf = this.getConf();
		var slots = conf.slots;
		for(slotIndex in slots) {
			var sessions = slots[slotIndex].sessions;
			for(sessionIndex in sessions) {
				var storedSession = sessions[sessionIndex];
				if(storedSession.id == session.id) {
					sessions[sessionIndex] = session;
				}
			}
		}
		this.setConf(conf);
	};

//========================================
//Communication
//========================================
	ProxyAPI = {};
	ProxyAPI.loadConfById = function(confId, callback) {
		var persistCallback = function(conf) {
			LocalStorageAPI.setConf(conf);
			callback(conf);
		};
		if(confId) {
			var selectedConf = LocalStorageAPI.getConf();
			if(selectedConf != null && selectedConf.id == confId) {
				console.log('load local conf: ' + conf.id);
				callback(selectedConf);
			} else {
				console.log('load remote conf');
				$.ajax({
					type : 'GET',
					dataType : 'json',
					contentType : 'application/json',
					url : '/xkeng/conference/'
							+ confId + '/slots',
					success : persistCallback,
					crossDomain : false,
					error : handleException
	
			});
			}
		} else {
			console.log('load initial conf');
			$.ajax({
				type : 'GET',
				dataType : 'json',
				contentType : 'application/json',
				url : '/xkeng/conference/next/1/slots',
				success : persistCallback,
				crossDomain : false,
				error : handleException
			})				;
		}
	}
		
		ProxyAPI.loadSessionById = function(sessId, callback, forceReload) {
				var selectedSession = LocalStorageAPI.getSession(sessId);
				if(selectedSession && !forceReload) {
					console.log('load local session: ' + sessId);
					callback(selectedSession)
				} else {
					console.log('load remote session');
					var callbacks = function(session) {
						$.mobile.hidePageLoadingMsg();
						LocalStorageAPI.setSession(session);
			 			callback(session);
		 			};
					$.mobile.showPageLoadingMsg();
					$.ajax({
						type : 'GET',
						dataType : 'json',
						contentType : 'application/json',
						url : '/xkeng/session/' + sessId,
						success : callbacks,
						crossDomain : false,
						error : handleException
					});
				}
		};
	ProxyAPI.loadConfSummary = function(pastcount, futurecount) {
		$.ajax({
			type : 'GET',
			dataType : 'json',
			contentType : 'application/json',
			url : '/xkeng/conferences/summary/pastcount/' + pastcount + '/futurecount/' + futurecount,
			success : conferenceSummarySuccessCallback,
			crossDomain : false,
			error : handleException
		});
	};
	ProxyAPI.rateSession = function(sessId, rating, callback) {
		 var jsonRequest = {};
			jsonRequest.rate = rating;
			$.ajax({
				type : 'POST',
				dataType : 'json',
				contentType : 'application/json',
				url : '/xkeng/feedback/' + sessId + '/rating' ,
				success :  callback,
				crossDomain : false,
				data : JSON.stringify(jsonRequest),
				error : handleException
			});
	};
	
	ProxyAPI.login = function(username, password, successCallback, failureCallback) {
		var jsonRequest = {};
		jsonRequest.username = username;
		jsonRequest.password = password;
		$.ajax({
			type : 'POST',
			dataType : 'json',
			contentType : 'application/json',
			url : '/xkeng/login',
			success : successCallback,
			crossDomain : false,
			data : JSON.stringify(jsonRequest),
			error : failureCallback
		});
	};
	
	ProxyAPI.logout = function(callback) {
		$.ajax({
			type : 'GET',
			url : '/xkeng/logout',
			success : callback,
			crossDomain : false,
			error : function(xhr, ajaxOptions, thrownError) {
				alert("Unable to login\nStatus Code " + xhr.status + "\nMessage: "
						+ thrownError );
			}
		});
	};

//========================================
//Utilities
//========================================
	var dateFormat = function () {
		var	token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,
			timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
			timezoneClip = /[^-+\dA-Z]/g,
			pad = function (val, len) {
				val = String(val);
				len = len || 2;
				while (val.length < len) val = "0" + val;
				return val;
			};

		// Regexes and supporting functions are cached through closure
		return function (date, mask, utc) {
			var dF = dateFormat;

			// You can't provide utc if you skip other args (use the "UTC:" mask prefix)
			if (arguments.length == 1 && Object.prototype.toString.call(date) == "[object String]" && !/\d/.test(date)) {
				mask = date;
				date = undefined;
			}

			// Passing date through Date applies Date.parse, if necessary
			date = date ? new Date(date) : new Date;
			if (isNaN(date)) throw SyntaxError("invalid date");

			mask = String(dF.masks[mask] || mask || dF.masks["default"]);

			// Allow setting the utc argument via the mask
			if (mask.slice(0, 4) == "UTC:") {
				mask = mask.slice(4);
				utc = true;
			}

			var	_ = utc ? "getUTC" : "get",
				d = date[_ + "Date"](),
				D = date[_ + "Day"](),
				m = date[_ + "Month"](),
				y = date[_ + "FullYear"](),
				H = date[_ + "Hours"](),
				M = date[_ + "Minutes"](),
				s = date[_ + "Seconds"](),
				L = date[_ + "Milliseconds"](),
				o = utc ? 0 : date.getTimezoneOffset(),
				flags = {
					d:    d,
					dd:   pad(d),
					ddd:  dF.i18n.dayNames[D],
					dddd: dF.i18n.dayNames[D + 7],
					m:    m + 1,
					mm:   pad(m + 1),
					mmm:  dF.i18n.monthNames[m],
					mmmm: dF.i18n.monthNames[m + 12],
					yy:   String(y).slice(2),
					yyyy: y,
					h:    H % 12 || 12,
					hh:   pad(H % 12 || 12),
					H:    H,
					HH:   pad(H),
					M:    M,
					MM:   pad(M),
					s:    s,
					ss:   pad(s),
					l:    pad(L, 3),
					L:    pad(L > 99 ? Math.round(L / 10) : L),
					t:    H < 12 ? "a"  : "p",
					tt:   H < 12 ? "am" : "pm",
					T:    H < 12 ? "A"  : "P",
					TT:   H < 12 ? "AM" : "PM",
					Z:    utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
					o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
					S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
				};

			return mask.replace(token, function ($0) {
				return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
			});
		};
	}();

	// Some common format strings
	dateFormat.masks = {
		"default":      "ddd mmm dd yyyy HH:MM:ss",
		shortDate:      "m/d/yy",
		mediumDate:     "mmm d, yyyy",
		longDate:       "mmmm d, yyyy",
		fullDate:       "dddd, mmmm d, yyyy",
		shortTime:      "h:MM TT",
		mediumTime:     "h:MM:ss TT",
		longTime:       "h:MM:ss TT Z",
		isoDate:        "yyyy-mm-dd",
		isoTime:        "HH:MM:ss",
		isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
		isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
	};

	// Internationalization strings
	dateFormat.i18n = {
		dayNames: [
			"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
			"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
		],
		monthNames: [
			"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
			"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
		]
	};

	// For convenience...
	Date.prototype.format = function (mask, utc) {
		return dateFormat(this, mask, utc);
	};

	
	Date.prototype.setISO8601 = function (string) {
	    var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
	        "(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?" +
	        "(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";
	    var d = string.match(new RegExp(regexp));

	    var offset = 0;
	    var date = new Date(d[1], 0, 1);

	    if (d[3]) { date.setMonth(d[3] - 1); }
	    if (d[5]) { date.setDate(d[5]); }
	    if (d[7]) { date.setHours(d[7]); }
	    if (d[8]) { date.setMinutes(d[8]); }
	    if (d[10]) { date.setSeconds(d[10]); }
	    if (d[12]) { date.setMilliseconds(Number("0." + d[12]) * 1000); }
	    if (d[14]) {
	        offset = (Number(d[16]) * 60) + Number(d[17]);
	        offset *= ((d[15] == '-') ? 1 : -1);
	    }

	    offset -= date.getTimezoneOffset();
	    time = (Number(date) + (offset * 60 * 1000));
	    this.setTime(Number(time));
	};
	
	function getFormattedDate(isoDateStr, format) {
		var date = toDate(isoDateStr);
		date.setISO8601(isoDateStr);
		return date.format(format);
	}
	
	function toDate(isoDateStr) {
		var date = new Date();
		date.setISO8601(isoDateStr);
		return date;
	}
	
	function splitText(text, chunkLength, separator) {
		if(text != null && text.length > chunkLength) {
			var finalText = "";
			var pattern = ".{1," + chunkLength + "}";
			var split = text.match(new RegExp(pattern, "gi"));
			for(splitIndex in split) {
				finalText += split[splitIndex] ;
				if(splitIndex < (split.length -1)) {
					finalText += separator;
				}
			}	
			return finalText;
		}
		return text;
	}
	
	
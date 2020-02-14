const TYPE_BYTE = 1;
const TYPE_SHORT = 2;
const TYPE_INT = 3;
const TYPE_LONG = 4;
const TYPE_OBJECT = 5;
const TYPE_ARRAY = 6;
const TYPE_STRING = 7;
const TYPE_NULL = 8;
const TYPE_ARRAY_NULL_BOUNDED = 9;
const TYPE_OBJECT_EMPTY_BOUNDED = 10;
const TYPE_BYTE_ARRAY = 11;
const TYPE_BOOLEAN_TRUE = 12;
const TYPE_BOOLEAN_FALSE = 13;
function readProgress(buffer) {
	let percent = buffer.idx / buffer.buffer.length;
	if (buffer.lastPercent == null || percent - buffer.lastPercent > 0.01) {
		buffer.lastPercent = percent;
		self.postMessage({
			type: 'progress',
			percent: percent
		});
	}
};
function readByte(buffer) {
	let b1 = buffer.buffer[buffer.idx++];
	if(b1 === undefined) {
		throw "EOF";
	}
	return b1;
};
function readShort(buffer) {
	let b1 = buffer.buffer[buffer.idx++];
	let b2 = buffer.buffer[buffer.idx++];
	if(b1 === undefined || b2 === undefined) {
		throw "EOF";
	}
	return (b1 << 8) | b2;
};
function readInt(buffer) {
	let b1 = buffer.buffer[buffer.idx++];
	let b2 = buffer.buffer[buffer.idx++];
	let b3 = buffer.buffer[buffer.idx++];
	let b4 = buffer.buffer[buffer.idx++];
	if(b1 === undefined || b2 === undefined || b3 === undefined || b4 === undefined) {
		throw "EOF";
	}
	return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
};
function readLong(buffer) {
	let b1 = buffer.buffer[buffer.idx++];
	let b2 = buffer.buffer[buffer.idx++];
	let b3 = buffer.buffer[buffer.idx++];
	let b4 = buffer.buffer[buffer.idx++];
	let b5 = buffer.buffer[buffer.idx++];
	let b6 = buffer.buffer[buffer.idx++];
	let b7 = buffer.buffer[buffer.idx++];
	let b8 = buffer.buffer[buffer.idx++];
	if(b1 === undefined || b2 === undefined || b3 === undefined || b4 === undefined ||
		b5 === undefined || b6 === undefined || b7 === undefined || b8 === undefined) {
		throw "EOF";
	}
	//needs multiplication as shifting is 32 bit only
	return ((b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32)) * 0x100000000 + 
		((b5 << 24) | (b6 << 16) | (b7 << 8) | b8);
};
function readString(buffer) {
	let len = readInt(buffer);
	let charbuf = new Array(len);
	for(let i = 0; i < len; i++) {
		let c = readShort(buffer);
		charbuf[i] = String.fromCharCode(c);
	}
	readProgress(buffer);
	return charbuf.join('');
};
function readObject(buffer) {
	let type = readByte(buffer);
	switch (type) {
		case TYPE_BYTE: {
			return readByte(buffer);
		}
		case TYPE_SHORT: {
			return readShort(buffer);
		}
		case TYPE_INT: {
			return readInt(buffer);
		}
		case TYPE_LONG: {
			return readLong(buffer);
		}
		case TYPE_OBJECT: {
			let len = readInt(buffer);
			let result = {
			};
			for(let i = 0; i < len; i++) {
				let name = readString(buffer);
				let value = readObject(buffer);
				result[name] = value;
			}
			readProgress(buffer);
			return result;
		}
		case TYPE_ARRAY: {
			let len = readInt(buffer);
			let array = new Array(len);
			for(let i = 0; i < len; i++) {
				array[i] = readObject(buffer);
			}
			readProgress(buffer);
			return array;
		}
		case TYPE_STRING: {
			let str = readString(buffer);
			readProgress(buffer);
			return str;
		}
		case TYPE_NULL: {
			return null;
		}
		case TYPE_ARRAY_NULL_BOUNDED: {
			let array = new Array();
			while (true) {
				let obj = readObject(buffer);
				if (obj == null) {
					break;
				}
				array.push(obj);
			}
			readProgress(buffer);
			return array;
		}
		case TYPE_OBJECT_EMPTY_BOUNDED: {
			let result = {
			};
			while (true) {
				let name = readString(buffer);
				if (name == "") {
					break;
				}
				let value = readObject(buffer);
				result[name] = value;
			}
			readProgress(buffer);
			return result;
		}
		case TYPE_BYTE_ARRAY: {
			let len = readInt(buffer);
			let result = buffer.buffer.slice(buffer.idx, buffer.idx + len);
			buffer.idx += len;
			
			return new TextDecoder().decode(result);
		}
		case TYPE_BOOLEAN_TRUE: {
			return true;
		}
		case TYPE_BOOLEAN_FALSE: {
			return false;
		}
		default: {
			throw "unknown-type " + type;
		}
	}
}

function parseInput(buffer) {
	let magic = readInt(buffer);
	if (magic != 0x45a8f96a) {
		throw "Invalid input";
	}
	let version = readInt(buffer);
	if (version != 1) {
		throw "Unsupported version " + version;
	}
	let bt = {
	};
	while (true) {
		let str;
		try {
			str = readString(buffer);
		} catch(e) {
			if (e != "EOF") {
				console.log("TODO read error: " + e);
			}
			break;
		}
		try {
			bt[str] = readObject(buffer);
		} catch(e) {
			throw "Read error " + e;
		}
	}
	function compareDuration(l, r){
		let ldur = l.end - l.start;
		let rdur = r.end - r.start;
		return rdur - ldur;
	};
	bt.tasks.sort(compareDuration);
	bt._tasks_by_trace_id = {
	};
	bt.tasks.forEach(function(task){
		if (task.trace_id != null) {
			bt._tasks_by_trace_id[task.trace_id] = task;
		}
		task._created_by = [];
		if (task.inner_tasks != null) {
			task.inner_tasks.sort(compareDuration);
		}
	});
	bt.tasks.forEach(function(task){
		if (task.created_tasks != null && task.created_tasks.length > 0) {
			task.created_tasks.forEach(function(createdtasktraceid){
				let createdtask = bt._tasks_by_trace_id[createdtasktraceid];
				if (createdtask == null) {
					return;
				}
				createdtask._created_by.push(task);
			});
		}
	});
	return bt;
}

self.addEventListener('message', function(e) {
	let bt;
	try {
		bt = parseInput(e.data);
	}catch (e) {
		self.postMessage({
			type: 'exception',
			exception: e
		});
		return;
	}
	self.postMessage({
		type: 'done',
		result: bt
	});
}, false);
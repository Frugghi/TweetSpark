$(document).ready(function() {
    $("#toc").toc({
	'highlightOffset': 1,
    });
    var params = URI(window.location.href).search(true);

    var user = params.user;
    var job = params.job;
    var flow = params.flow;
    var stage = params.stage;
    var phase = params.phase;
    var jvmName = params.jvm;
    var base = params.prefix || $('#prefix').text() || 'bigdata.profiler';
    var refresh = params.refresh || $('#refresh').text() || 60;
    var optionalJvmName = jvmName && jvmName !== '' ? '/' + jvmName + '/' : '/';

    var prefix = base + '.' + user + '.' + job + '.' + flow + '.' + stage + '.' + phase;
    var cpuPrefix = 'cpu.trace';
    var heapPrefix = 'heap';
    var nonHeapPrefix = 'nonheap';
    var finalizePrefix = 'pending-finalization-count';
    var gcPrefix = 'gc';
    var classLoadingPrefix = '.*class-count';

    var memoryMetrics = [{metric:'init', alias:'Initial'},{metric:'committed', alias:'Committed'},{metric:'max', alias:'Maximum'},{metric:'used', alias:'Used'}];
    var finalizeMetrics = [{metric: finalizePrefix, alias: 'Objects Pending Finalization'}];
    var gcCountMetrics = [{metric:'PS_MarkSweep.count', alias:'PS_MarkSweep'},{metric:'PS_Scavenge.count', alias:'PS_Scavenge'}];
    var gcTimeMetrics = [{metric:'PS_MarkSweep.time', alias:'PS_MarkSweep'},{metric:'PS_Scavenge.time', alias:'PS_Scavenge'}];
    var gcRuntimeMetrics = [{metric:'PS_MarkSweep.runtime', alias:'PS_MarkSweep'},{metric:'PS_Scavenge.runtime', alias:'PS_Scavenge'}];
    var classLoadingMetrics = [{metric:'loaded-class-count', alias:'Classes Loaded'},{metric:'total-loaded-class-count', alias:'Total Classes Loaded'},{metric:'unloaded-class-count', alias:'Classes Unloaded'}];
    var heapPools = [{pool:'ps-eden-space', selector:'#eden', title:'Eden'}, {pool:'ps-old-gen', selector:'#oldgen', title:'Old Generation'}, {pool:'ps-survivor-space', selector:'#survivor', title:'Survivor Space'}];
    var nonHeapPools = [{pool:'code-cache', selector:'#codecache', title:'Code Cache'}, {pool:'ps-perm-gen', selector:'#permgen', title:'Permgen'}];

    $("#toc ul").append('<li class=toc-h2><a href=/cpu/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + optionalJvmName + cpuPrefix + ' target=_blank>Flame Graph</a></li>');
    $('#toc').affix({
	offset: {
	    top: $('.navbar navbar-default').height()
	}
    });

    var heapGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + optionalJvmName + heapPrefix);
    var nonHeapGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + optionalJvmName + nonHeapPrefix);
    var finalizeGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + optionalJvmName + finalizePrefix);
    var gcGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + optionalJvmName + gcPrefix);
    var classLoadingGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + optionalJvmName + classLoadingPrefix);

    $.when(heapGet).done(function() {
	var heapResults = heapGet['responseJSON'];
	ViewUtil.renderGraph(heapResults, 'Heap Usage', '#heap', ViewUtil.getMetricsForPool('total', memoryMetrics));
	heapPools.forEach(function(pool) {
	    ViewUtil.renderGraph(heapResults, pool.title, pool.selector, ViewUtil.getMetricsForPool(pool.pool, memoryMetrics));
	});
    });

    $.when(nonHeapGet).done(function() {
	var nonHeapResults = nonHeapGet['responseJSON'];
	ViewUtil.renderGraph(nonHeapResults, 'Non-Heap Usage', '#nonheap', ViewUtil.getMetricsForPool('total', memoryMetrics));
	
	nonHeapPools.forEach(function(pool) {
	    ViewUtil.renderGraph(nonHeapResults, pool.title, pool.selector, ViewUtil.getMetricsForPool(pool.pool, memoryMetrics));
	});
    });

    $.when(finalizeGet).done(function() {
	var finalizeResults = finalizeGet['responseJSON'];
	ViewUtil.renderGraph(finalizeResults, 'Objects Pending Finalization', '#finalize', finalizeMetrics);
    });

    $.when(gcGet).done(function() {
	var gcResults = gcGet['responseJSON'];
	ViewUtil.renderGraph(gcResults, 'Garbage Collection', '#count', gcCountMetrics);
	ViewUtil.renderGraph(gcResults, 'Garbage Collection', '#time', gcTimeMetrics);
	ViewUtil.renderGraph(gcResults, 'Garbage Collection', '#runtime', gcRuntimeMetrics);
    });

    $.when(classLoadingGet).done(function() {
	var classLoadingResults = classLoadingGet['responseJSON'];
	ViewUtil.renderGraph(classLoadingResults, 'Class Loading', '#classloading', classLoadingMetrics);
    });
    
    if (refresh > 0) {
	setTimeout(function() {
	    location.reload();
	}, refresh * 1000);
    }
});

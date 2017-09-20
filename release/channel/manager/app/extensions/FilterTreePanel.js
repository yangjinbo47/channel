Ext.define('CMS.extensions.FilterTreePanel', {
	extend: 'Ext.tree.Panel',
    mixins: {
       treeFilter: 'CMS.extensions.TreeFilter'
    }
});
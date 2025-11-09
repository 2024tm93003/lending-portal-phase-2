/**
 * EquipmentCatalog
 *
 * Displays a list/grid of equipment tiles and provides simple filtering controls.
 *
 * Props:
 * @param {Array<Object>} items - Array of equipment objects to render. Each item should include { id, itemName, category, conditionNote, availableQuantity, totalQuantity }.
 * @param {Object} filters - Current filter values: { category, availableOnly }.
 * @param {function(Object):void} onFiltersChange - Called with updated filters when user changes any filter control.
 */
const EquipmentCatalog = ({ items, filters, onFiltersChange }) => {
  /**
   * Merge and emit filter changes.
   * @param {Object} changes - Partial filter properties to merge into current filters.
   */
  const updateFilters = (changes) => {
    onFiltersChange({ ...filters, ...changes });
  };

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <h2 className="text-xl font-semibold">Equipment Catalog</h2>
      <div className="mt-4 flex flex-wrap gap-4 items-center">
        <div className="flex-1 min-w-[200px]">
          <label className="block text-sm font-medium text-slate-700">Filter by category</label>
          <input
            value={filters.category}
            onChange={(event) => updateFilters({ category: event.target.value })}
            className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2"
          />
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input
            type="checkbox"
            checked={filters.availableOnly}
            onChange={(event) => updateFilters({ availableOnly: event.target.checked })}
            className="h-4 w-4 rounded border-slate-300"
          />
          <span>only show available</span>
        </label>
      </div>

      <div className="mt-5 grid gap-4 grid-cols-1 sm:grid-cols-2">
        {items.map((equip) => (
          <div key={equip.id} className="border rounded-lg p-4 bg-white shadow-sm">
            <h3 className="text-lg font-semibold">{equip.itemName}</h3>
            <p className="text-sm text-slate-500">{equip.category}</p>
            <p className="mt-2 text-sm text-slate-700">{equip.conditionNote}</p>
            <p className="text-sm text-slate-500 mt-3">{equip.availableQuantity} free / {equip.totalQuantity} total</p>
          </div>
        ))}
        {items.length === 0 && <p className="text-slate-600">No equipment listed yet.</p>}
      </div>
    </div>
  );
};

export default EquipmentCatalog;

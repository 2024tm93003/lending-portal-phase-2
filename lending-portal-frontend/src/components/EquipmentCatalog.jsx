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
    <div className="cardy">
      <h2>Equipment Catalog</h2>
      <div className="filterRow">
        <div className="filterGroup">
          <label>Filter by category</label>
          <input value={filters.category} onChange={(event) => updateFilters({ category: event.target.value })} />
        </div>
        <label className="checkboxRow">
          <input
            type="checkbox"
            checked={filters.availableOnly}
            onChange={(event) => updateFilters({ availableOnly: event.target.checked })}
          />
          only show available
        </label>
      </div>
      <div className="equipGrid">
        {items.map((equip) => (
          <div key={equip.id} className="equipTile">
            <h3>{equip.itemName}</h3>
            <p className="tiny">{equip.category}</p>
            <p>{equip.conditionNote}</p>
            <p className="tiny">
              {equip.availableQuantity} free / {equip.totalQuantity} total
            </p>
          </div>
        ))}
        {items.length === 0 && <p>No equipment listed yet.</p>}
      </div>
    </div>
  );
};

export default EquipmentCatalog;

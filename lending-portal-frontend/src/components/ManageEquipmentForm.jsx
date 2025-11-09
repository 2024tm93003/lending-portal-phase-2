/**
 * ManageEquipmentForm
 *
 * Simple form used by administrators to add or edit equipment items.
 * Controlled by `formState` and emits updates via `onChange`.
 *
 * Props:
 * @param {Object} formState - Equipment form values: { itemName, category, conditionNote, totalQuantity, availableQuantity }.
 * @param {function(Object):void} onChange - Called with updated form state when a field changes.
 * @param {function(Event):void} onSubmit - Form submit handler.
 */
const ManageEquipmentForm = ({ formState, onChange, onSubmit }) => {
  /**
   * Create a change handler for a specific form field.
   * @param {string} field - Field to update.
   * @returns {function(Event):void}
   */
  const updateField = (field) => (event) => {
    onChange({ ...formState, [field]: event.target.value });
  };

  return (
    <div className="cardy">
      <h2>Add equipment</h2>
      <form onSubmit={onSubmit}>
        <label>Name</label>
        <input value={formState.itemName} onChange={updateField("itemName")} required />
        <label>Category</label>
        <input value={formState.category} onChange={updateField("category")} required />
        <label>Condition Notes</label>
        <textarea rows={3} value={formState.conditionNote} onChange={updateField("conditionNote")} />
        <label>Total Quantity</label>
        <input type="number" value={formState.totalQuantity} min="1" onChange={updateField("totalQuantity")} />
        <label>Available Now</label>
        <input value={formState.availableQuantity ?? ""} type="number" onChange={updateField("availableQuantity")} />
        <button type="submit" className="btn btnPrimary btnFull">
          Save Item
        </button>
      </form>
    </div>
  );
};

export default ManageEquipmentForm;

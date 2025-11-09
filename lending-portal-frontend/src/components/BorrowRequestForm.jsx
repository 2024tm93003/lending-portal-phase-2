/**
 * BorrowRequestForm
 *
 * A controlled form component used to submit a borrow request for equipment.
 * This component is presentation-focused and delegates state updates and
 * submission handling to the parent via the `onChange` and `onSubmit` props.
 *
 * Props:
 * @param {Array<Object>} items - Array of equipment objects to choose from. Each item should include { id, itemName, availableQuantity }.
 * @param {Object} formState - Current form values: { equipmentId, startDate, endDate, qty }.
 * @param {function(Object):void} onChange - Handler called with the new form state when any field changes.
 * @param {function(Event):void} onSubmit - Form submit handler.
 *
 * Returns JSX form elements.
 */
const BorrowRequestForm = ({ items, formState, onChange, onSubmit }) => {
  /**
   * Create a change handler for a specific field.
   * @param {string} field - The form field name to update (e.g. 'startDate').
   * @returns {function(Event):void} - Event handler which calls `onChange` with updated state.
   */
  const updateField = (field) => (event) => {
    onChange({ ...formState, [field]: event.target.value });
  };

  return (
    <div className="cardy">
      <h2>Borrow something</h2>
      <form onSubmit={onSubmit}>
        <label>Select item</label>
        <select value={formState.equipmentId} onChange={updateField("equipmentId")} required>
          <option value="">-- Choose --</option>
          {items.map((equip) => (
            <option key={equip.id} value={equip.id}>
              {equip.itemName} ({equip.availableQuantity} free)
            </option>
          ))}
        </select>
        <label>Start Date</label>
        <input type="date" value={formState.startDate} onChange={updateField("startDate")} required />
        <label>End Date</label>
        <input type="date" value={formState.endDate} onChange={updateField("endDate")} required />
        <label>Quantity</label>
        <input type="number" min="1" value={formState.qty} onChange={updateField("qty")} />
        <button type="submit" className="btn btnPrimary btnFull">
          Submit Request
        </button>
      </form>
    </div>
  );
};

export default BorrowRequestForm;

const RequestTable = ({ requests, role, onDecision }) => {
  const isManager = role === "STAFF" || role === "ADMIN";
  const emptyColSpan = isManager ? 7 : 6;

  return (
    <div className="cardy">
      <h2>Borrow Requests</h2>
      <table className="listy">
        <thead>
          <tr>
            <th>ID</th>
            <th>Item</th>
            <th>Borrower</th>
            <th>Duration</th>
            <th>Qty</th>
            <th>Status</th>
            {isManager && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {requests.map((request) => (
            <tr key={request.id}>
              <td>{request.id}</td>
              <td>{request.gear?.itemName || "-"}</td>
              <td>{request.requester?.displayName || request.requester?.username}</td>
              <td>
                {request.startDate} → {request.endDate}
              </td>
              <td>{request.qty}</td>
              <td>
                <span className={`pill ${request.status}`}>{request.status}</span>
              </td>
              {isManager && (
                <td className="actions">
                  {request.status === "PENDING" && (
                    <>
                      <button className="btn btnPrimary" onClick={() => onDecision(request.id, "approve")}>
                        Approve
                      </button>
                      <button className="btn btnDanger" onClick={() => onDecision(request.id, "reject", "not available")}>
                        Reject
                      </button>
                    </>
                  )}
                  {request.status === "APPROVED" && (
                    <button className="btn btnPrimary" onClick={() => onDecision(request.id, "issue")}>
                      Mark Issued
                    </button>
                  )}
                  {request.status === "ISSUED" && (
                    <button className="btn btnPrimary" onClick={() => onDecision(request.id, "return")}>
                      Close Return
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
          {requests.length === 0 && (
            <tr>
              <td colSpan={emptyColSpan} className="tableEmpty">
                No requests found.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default RequestTable;

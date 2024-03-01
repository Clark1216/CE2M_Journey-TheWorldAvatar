from enum import Enum
from typing import Optional, Tuple

from pydantic.dataclasses import dataclass

class NumericalOperator(Enum):
    EQ = "="
    GT = ">"
    GE = ">="
    LT = "<"
    LE = "<="


@dataclass
class AtomicNumericalConstraint:
    operator: NumericalOperator
    operand: float
    unit: Optional[str] = None

    def __str__(self):
        result = "{operator} {operand}".format(
            operator=self.operator.value, operand=self.operand
        )
        if self.unit:
            result += self.unit
        return result


class LogicalOperator(Enum):
    AND = "&&"
    OR = "||"


@dataclass
class CompoundNumericalConstraint:
    logical_operator: Optional[LogicalOperator]
    constraints: Tuple[AtomicNumericalConstraint, ...]

    def __str__(self):
        if not self.constraints:
            return ""

        if not self.logical_operator:
            return str(self.constraints[0])

        if self.logical_operator is LogicalOperator.AND:
            delimiter = " and "
        else:
            delimiter = " or "
        return delimiter.join([str(x) for x in self.constraints])

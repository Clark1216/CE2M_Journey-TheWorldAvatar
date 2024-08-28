from pydantic import BaseModel, Field


class XYZAtom(BaseModel):
    symbol: str
    x: float
    y: float
    z: float

    def __str__(self):
        return f"{self.symbol} {self.x} {self.y} {self.z}"


class XYZ(BaseModel):
    comment: str
    atoms: list[XYZAtom]

    def to_xyz_str(self):
        return """{number_of_atoms}
{comment_line}
{atoms}""".format(
            number_of_atoms=len(self.atoms),
            comment_line=self.comment,
            atoms="\n".join(str(atom) for atom in self.atoms),
        )


class CIFUnitCell(BaseModel):
    a: float
    b: float
    c: float
    alpha: float
    beta: float
    gamma: float

    def __str__(self):
        return f"""_cell_length_a      {self.a}
_cell_length_b      {self.b}
_cell_length_c      {self.c}
_cell_angle_alpha   {self.alpha}
_cell_angle_beta    {self.beta}
_cell_angle_gamma   {self.gamma}"""


class CIFAtomSite(BaseModel):
    label: str
    symbol: str
    fract_x: float
    fract_y: float
    fract_z: float

    def __str__(self):
        return (
            f"{self.label} {self.symbol} {self.fract_x} {self.fract_y} {self.fract_z}"
        )


class CIF(BaseModel):
    name: str
    unit_cell: CIFUnitCell
    atoms: list[CIFAtomSite] = Field(..., min_items=1)

    def to_cif_str(self):
        return """data_{name}

#########################################################
#                                                       
# CIF file for zeolite {name} generated by ZeoliteAgent 
#                                                       
#########################################################

{unit_cell_params}

loop_
  _atom_site_label
  _atom_site_type_symbol
  _atom_site_fract_x
  _atom_site_fract_y
  _atom_site_fract_z
{atoms}""".format(
            name=self.name,
            unit_cell_params=self.unit_cell,
            atoms="\n".join(str(atom) for atom in self.atoms),
        )
